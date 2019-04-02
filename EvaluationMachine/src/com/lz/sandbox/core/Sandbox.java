package com.lz.sandbox.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.google.gson.Gson;

import com.lz.sandbox.callable.ProblemCallable;
import com.lz.sandbox.constant.CommunicationSignal;
import com.lz.sandbox.constant.ConstantParameter;
import com.lz.sandbox.core.classLoader.SandboxClassLoader;
import com.lz.sandbox.core.securityManager.SandboxSecurityManager;
import com.lz.sandbox.core.systemInStream.ThreadInputStream;
import com.lz.sandbox.core.systemOutStream.CacheOutputStream;
import com.lz.sandbox.dto.Problem;
import com.lz.sandbox.dto.ProblemResult;
import com.lz.sandbox.dto.ProblemResultItem;
import com.lz.sandbox.dto.Request;
import com.lz.sandbox.dto.Response;
import com.lz.sandbox.dto.SandBoxStatus;
import com.lz.sandbox.dto.SandboxInitData;

/**
 * 测评机
 * @author 刘铮
 */
public class Sandbox {
	/**
	 * 每加载超过5个类后，就替换一个新的ClassLoader
	 */
	private static final int UPDATE_CLASSLOADER_GAP = 5;
	/**
	 * 记录一共加载过的类数量
	 */
	private int loadedClassCount = 0;
	private SandboxInitData sandboxInitData;
	private String pid = null;

	/**
	 * 与前台通信的Socker实例对象
	 */
	private ServerSocket serverSocket;
	private Socket communicateSocket;
	private SandboxClassLoader sandboxClassLoader;
	private Gson gson = null;
	/**
	 * 管理JVM内存的MXbean
	 */
	private MemoryMXBean systemMemoryBean = null;
	private long beginStartTime = 0;
	/**
	 * 表示当前进程是否在忙，如果在忙的话，就表示当前正在判题(这是当前正在的忙情况，以后可能会增加更多的情况)
	 */
	private boolean isBusy = false;

	/**
	 * 一个题目的处理规则
	 */
	private ProblemCallable problemCallable;

	/**
	 * 所有代码的所有运行结果都将保存在内存里
	 */
	private volatile CacheOutputStream resultBuffer = new CacheOutputStream();

	/**
	 * 当前Sandbox线程输入流
	 */
	private volatile ThreadInputStream systemThreadIn = new ThreadInputStream();

	/**
	 * 用一个线程池去处理每个判题请求，这种线程池是单线程，保证所有任务按顺序执行(FIFO,LIFO,优先级)
	 */
	private ExecutorService problemThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setName("problemThreadPool");
					thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
						@Override
						public void uncaughtException(Thread t, Throwable e) {
							response(null, CommunicationSignal.ResponseSignal.ERROR,
									null, e.getMessage());
						}
					});
					return thread;
				}
			});

	/**
	 * 用一个线程池去等待每个判题请求的结果返回
	 */
	private ExecutorService problemResultThreadPool = Executors
			.newSingleThreadExecutor(new ThreadFactory() {

				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setName("problemResultThreadPool");
					thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
						@Override
						public void uncaughtException(Thread t, Throwable e) {
							response(null,
									CommunicationSignal.ResponseSignal.ERROR,
									null, e.getMessage());
						}
					});
					return thread;
				}
			});

	public static void main(String[] args) {
		new Sandbox(args);
	}

	private Sandbox(String[] args) {
		initSandbox(args);
	}

	/**
	 * 沙箱初始化函数
	 * @param args 初始化参数
	 */
	private void initSandbox(String[] args) {
		// 获取进程id，用于向外界反馈
		getPid();
		// 沙箱环境准备：初始化沙箱数据和获得管理JVM内存系统的bean
		SandboxInitData sandboxInitData = prepareBuildingNeed(args[0]);
		// 打开用于与外界沟通的通道，没有连接将会一直阻塞，连接完成才会往下执行
		openServerSocketWaitToConnect(sandboxInitData.getPort());
		// 确保能与外界沟通之后，才开始准备执行class文件的环境，包括重定向标准输入和输出流
		buildEnvironment(sandboxInitData);
		// 等外界与沙箱，通过socket沟通上之后，就会进行业务上的沟通
		service();

	}

	/**
	 * 获取进程ID
	 */
	private void getPid() {
		//获得管理的JVM运行时MXbean，以此获得进程号
		String name = ManagementFactory.getRuntimeMXBean().getName();
		pid = name.split("@")[0];
	}
	/**
	 * 准备沙箱初始化必要内容
	 * @param sandboxInitJson 沙箱初始化JSON格式数据
	 * @return 沙箱初始化所需要的信息
	 */
	private SandboxInitData prepareBuildingNeed(String sandboxInitJson) {
		gson = new Gson();
		SandboxInitData sandboxInitData = gson.fromJson(sandboxInitJson, SandboxInitData.class);
		//获得管理JVM内存系统的bean，通过这个bean可以管理和监控JVM系统
		systemMemoryBean = ManagementFactory.getMemoryMXBean();
		this.sandboxInitData = sandboxInitData;
		return sandboxInitData;
	}

	/**
	 * 打开连接，等待建立连接
	 * @param port 监听端口
	 */
	private void openServerSocketWaitToConnect(int port) {

		try {
			serverSocket = new ServerSocket(port);
			System.out.println("sandbox" + port + "wait");
			communicateSocket = serverSocket.accept();
			System.out.println("pid:" + pid);
			// 只与外部建立一个沟通的连接
			serverSocket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			throw new RuntimeException("无法打开沙箱端Socket，可能是端口被占用了");
		}
	}

	/**
	 * 建立测评机环境
	 * @param sandboxInitData 测评机初始化信息
	 */
	private void buildEnvironment(SandboxInitData sandboxInitData) {
		sandboxClassLoader = new SandboxClassLoader(sandboxInitData.getClassFileRootPath());
		beginStartTime = System.currentTimeMillis();
		// 重定向输出流，此后程序中的所有运行结果都打印到内存中
		System.setOut(new PrintStream(resultBuffer));
		// 重定向输入流，此后程序中再又出现输入数据操作时，将会从ThreadInputStream流实例中获取数据
		System.setIn(systemThreadIn);
	}

	/**
	 * 系统服务函数
	 * 步骤：1、从前台获取到Json格式的流
	 * 		2、将Json格式流数据解析并分装一个Request Javabean
	 * 	 	3、转发请求给测评机，测评机处理请求
	 */
	private void service() {
		try {
			//从网络传输上获得输入流，这个流只有一行，所有的请求信息都放在了一行，最后以\结束。这个流包含了指令、题目信息、信号id信息
			Scanner scanner = new Scanner(communicateSocket.getInputStream());
			// 必须建立了连接和流之后，才能设置这里的权限
			System.setSecurityManager(new SandboxSecurityManager());
			String data = null;
			while (scanner.hasNext()) {
				// 每一次交流，都是一行一行的形式交流，即本次沟通内容发送完之后，发送方会在最后，加上一个"\n"，表示发送完了这条消息
				data = scanner.nextLine();
				//把读取到Json格式的流信息解析封装成Request对象
				Request request = gson.fromJson(data, Request.class);
				//分发请求
				dispatchRequest(request);
			}
			scanner.close();
		} catch (Exception e) {
			response(null, CommunicationSignal.ResponseSignal.ERROR, null,
					e.getMessage());
		}
	}

	/**
	 * 根据前台传送过来的指令，分发请求，测评机做出相应的响应
	 * @param request 请求内容
	 * @throws IOException 
	 */
	private void dispatchRequest(Request request) throws IOException {
		if (CommunicationSignal.RequestSignal.CLOSE_SANDBOX.equals(request.getCommand())) {
			closeSandboxService(request.getSignalId());
		} else if (CommunicationSignal.RequestSignal.SANDBOX_STATUS.equals(request.getCommand())) {
			feedbackSandboxStatusService(request.getSignalId());
		} else if (CommunicationSignal.RequestSignal.REQUSET_JUDGED_PROBLEM.equals(request.getCommand())) {
			// 防止内存使用过多
			if (loadedClassCount >= UPDATE_CLASSLOADER_GAP) {
				loadedClassCount = 0;
				// 重置类加载器，使得原有已经加载进内存的过期的类，可以得以释放
				sandboxClassLoader = new SandboxClassLoader(sandboxInitData.getClassFileRootPath());
				System.gc();
			}
			Future<List<ProblemResultItem>> processProblem = processProblem(request.getData());
			responseEvaluationResult(request.getSignalId(), processProblem);
			loadedClassCount++;
		} else if (CommunicationSignal.RequestSignal.IS_BUSY.equals(request.getCommand())) {
			checkBusy(request.getSignalId());
		}
	}

	/**
	 * 关闭沙箱服务
	 * @param signalId 关闭信号
	 */
	private void closeSandboxService(String signalId) {
		response(signalId, CommunicationSignal.ResponseSignal.OK,
				CommunicationSignal.RequestSignal.CLOSE_SANDBOX, null);
		try {
			communicateSocket.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		closeSandbox();
	}

	/**
	 * 返回沙箱状态的服务
	 * @param signalId 信号
	 */
	private void feedbackSandboxStatusService(String signalId) {
		SandBoxStatus sandBoxStatus = new SandBoxStatus();
		sandBoxStatus.setPid(pid);
		sandBoxStatus.setBeginStartTime(beginStartTime);
		sandBoxStatus.setBusy(isBusy);
		// 由堆内存和非堆内存组成
		long useMemory = systemMemoryBean.getHeapMemoryUsage().getUsed()
				+ systemMemoryBean.getNonHeapMemoryUsage().getUsed();
		sandBoxStatus.setUseMemory(useMemory);
		// 由堆内存和非堆内存组成
		long maxMemory = systemMemoryBean.getHeapMemoryUsage().getMax()
				+ systemMemoryBean.getNonHeapMemoryUsage().getMax();
		sandBoxStatus.setMaxMemory(maxMemory);
		response(signalId, CommunicationSignal.ResponseSignal.OK,
				CommunicationSignal.RequestSignal.SANDBOX_STATUS,
				gson.toJson(sandBoxStatus));

	}

	/**
	 * 进行任务处理
	 * @param problemJson 题目内容的JSON格式
	 * @return 题目处理结果
	 */
	private Future<List<ProblemResultItem>> processProblem(String problemJson) {
		Problem problem = gson.fromJson(problemJson, Problem.class);
		try {
			Class<?> mainClass = sandboxClassLoader.loadSandboxClass(problem.getClassFileName());
			Method mainMethod = mainClass.getMethod("main", String[].class);
			if (!Modifier.isStatic(mainMethod.getModifiers())) {
				throw new Exception("main方法不是静态方法");
			}

			mainMethod.setAccessible(true);
			problemCallable = new ProblemCallable(mainMethod, problem, resultBuffer, systemThreadIn);
			Future<List<ProblemResultItem>> submit = problemThreadPool.submit(problemCallable);
			isBusy = true;
			mainClass = null;
			return submit;
		} catch (ClassNotFoundException e) {
			response(null, CommunicationSignal.ResponseSignal.ERROR, null,
					e.getMessage());
		} catch (Exception e) {
			response(null, CommunicationSignal.ResponseSignal.ERROR, null,
					e.getMessage());
		}
		return null;
	}

	/**
	 * 检查沙箱是否正忙
	 * @param signalId 信号量
	 */
	private void checkBusy(String signalId) {
		String responseCommand = null;

		if (isBusy) {
			responseCommand = CommunicationSignal.ResponseSignal.YES;
		} else {
			responseCommand = CommunicationSignal.ResponseSignal.NO;
		}

		response(signalId, responseCommand,
				CommunicationSignal.RequestSignal.IS_BUSY, null);
	}

	/**
	 * 返回题目运行结果
	 * @param signalId 信号
	 * @param processProblem 题目运行结果
	 */
	private void responseEvaluationResult(final String signalId, final Future<List<ProblemResultItem>> processProblem) {
		problemResultThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (processProblem == null) {
					return;
				}
				try {
					List<ProblemResultItem> resultItems = processProblem.get();
					Problem problem = problemCallable.getProblem();
					ProblemResult problemResult = new ProblemResult();
					problemResult.setRunId(problem.getRunId());
					problemResult.setResultItems(resultItems);

					response(signalId, CommunicationSignal.ResponseSignal.OK,
							CommunicationSignal.RequestSignal.REQUSET_JUDGED_PROBLEM,
							gson.toJson(problemResult));
					isBusy = false;
					problemCallable = null;

					// 通知对方，主动告诉对方，自己已经空闲了，已经准备好下一次判题
					response(null, CommunicationSignal.ResponseSignal.IDLE, null, null);
				} catch (Exception e) {
					response(null, CommunicationSignal.ResponseSignal.ERROR, null,
							e.getMessage());
				}
			}
		});
	}

	/**
	 * 发送回复
	 * @param signalId 信号
	 * @param responseCommand 回复的命令
	 * @param requestCommand 请求的命令
	 * @param data 数据
	 */
	private void response(String signalId, String responseCommand,
						  String requestCommand, String data) {
		try {
			OutputStream outputStream = communicateSocket.getOutputStream();
			Response response = new Response();
			response.setSignalId(signalId);
			response.setResponseCommand(responseCommand);
			response.setRequestCommand(requestCommand);
			response.setData(data);
			outputStream.write((gson.toJson(response) + "\n").getBytes("UTF-8"));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			throw new RuntimeException("无法对外输出数据");
		}

	}

	/**
	 * 关闭沙箱
	 */
	private void closeSandbox() {
		try {
			communicateSocket.close();
		} catch (IOException e) {
		}
		System.exit(ConstantParameter.EXIT_VALUE);
	}
}

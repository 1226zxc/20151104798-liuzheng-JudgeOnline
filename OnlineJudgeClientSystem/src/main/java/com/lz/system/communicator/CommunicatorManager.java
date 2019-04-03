package com.lz.system.communicator;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.lz.system.communicator.listener.EvaluationMachineHandler;
import com.lz.system.dto.CommonRequest;
import com.lz.system.dto.CommunicatorStatus;
import com.lz.system.dto.JavaSandboxStartInfo;
import com.lz.system.dto.JudgeProblemRequest;
import com.lz.system.sandbox.dto.SandboxInitData;
import com.lz.util.Log4JUtil;
import com.lz.web.util.ThreadFactoryUtil;

import com.google.gson.Gson;

/**
 * 通信器管理器
 * 负责与后台测评机的通信操作，例如处理判题请求、
 * 测评机开启、测评机调度、测评机关闭等操作。
 *
 * @author 刘铮
 */
public class CommunicatorManager {
	/**
	 * 存储正在处理判题任务的通信器
	 */
	private BlockingQueue<Communicator> judgingCommunicators = new LinkedBlockingQueue<Communicator>();

	/**
	 * 存储空闲中测评机的通信器
	 */
	private BlockingQueue<Communicator> noJudgingCommunicators = new LinkedBlockingQueue<Communicator>();
	private BlockingQueue<Communicator> haveStopCommunicators = new LinkedBlockingQueue<Communicator>();
	/**
	 * 判题请求队列，保存着未处理的判题任务
	 */
	private BlockingQueue<JudgeProblemRequest> problemRequests = new LinkedBlockingQueue<>(16);
	private BlockingQueue<JudgeProblemRequest> highPriorityProblemRequests = new LinkedBlockingQueue<JudgeProblemRequest>();

	/**
	 * 注意，这个map仅仅是为了执行非请求判题的操作（比如，检查沙箱状态等）方便而设立，绝对不要从里面获取交流者，然后用于提交判题请求
	 */
	private Map<String, Communicator> allCommunicators = new ConcurrentHashMap<String, Communicator>();
	private ExecutorService watchingExecutor = Executors.newSingleThreadExecutor(ThreadFactoryUtil
					.getLogThreadFactory(CommunicatorManager.class.getName()
							+ " watchingExecutor "));

	private static volatile CommunicatorManager communicatorManager = null;

	/**
	 * 主要用于当题目已经取出了，但是暂时没有沙箱可以处理时，精确显示等待判题显示的显示问题
	 */
	private int wantHandlePrblemCount = 0;

	public static CommunicatorManager getInstance() {
		if (communicatorManager == null) {
			synchronized (CommunicatorManager.class) {
				if (communicatorManager == null) {
					communicatorManager = new CommunicatorManager();
				}
			}
		}

		return communicatorManager;
	}

	private CommunicatorManager() {
		init();
	}

	/**
	 * 从请求判题队列中获取任务，发送到空闲的测评机
	 * 如果当前线程没有被中断，那么处理此项任务的线程
	 * 将会在应用程序运行期间一直工作下去。虽然处理此
	 * 项任务复用这一个线程，但多线程处理此项任务还是
	 * 有必要的。实现此种监听方式是多线程轮询查询
	 */
	private void init() {
		// TODO 现在的代码还是挺难看的，到时候把一些东西抽取出来变成一个个方法以及变成一个Runnable这样吧
		watchingExecutor.execute(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					JudgeProblemRequest request = null;
					try {
						// 如果有空闲的沙箱的话，就取出一个判题请求，先取出优先级高的，这里并不会阻塞
						request = highPriorityProblemRequests.poll();

						if (request == null) {
							// 当前没有判题请求的话，则会一直阻塞在这里
							request = problemRequests.take();
						}
						wantHandlePrblemCount++;
					} catch (InterruptedException e) {
						continue;
					}

					Communicator communicator = null;
					try {
						// 取出一个空闲测评机的交流器
						// 如果当前阻塞队列中没有空闲的测评机器交流器，则会一直阻塞在这里，直到出现一个
						communicator = noJudgingCommunicators.take();
						wantHandlePrblemCount--;
					} catch (InterruptedException e) {
						Log4JUtil.logError(e);
					}

					// 移进判题阻塞队列中
					judgingCommunicators.add(communicator);
					// 向测评机发送请求数据
					communicator.sendRequest(request.getRequest(), request.getExecutor());
					communicator.setJudging(true);
					communicator = null;
				}
			}
		});
	}

	/**
	 * 返回一个交流者的身份区别凭证
	 * 
	 * @param sandboxStartInfo
	 * @return
	 */
	public String makeNewSandBox(JavaSandboxStartInfo sandboxStartInfo) {
		try {
			// 开启一个新的测评机，返回包含进程号的Process进程对象
			Process process = openNewSandBox(sandboxStartInfo);
			// 根据进程号连接到新创建的测评机
			return connectToNewMachine(sandboxStartInfo.getIp(), sandboxStartInfo.getPort(), process);
		} catch (Exception e) {
			Log4JUtil.logError(e);
			return null;
		}
	}

	/**
	 * 开启一个新的测评机，返回包含进程号的Process进程对象
	 * @param sandboxStartInfo 测评机机启动基本信息
	 * @return 返回包含进程号的Process进程对象
	 * @throws IOException
	 */
	private Process openNewSandBox(JavaSandboxStartInfo sandboxStartInfo)
			throws IOException {
		// 设置测评机初始化信息，通过通信传递给测评机初始化
		SandboxInitData sandboxInitData = new SandboxInitData();
		sandboxInitData.setPort(sandboxStartInfo.getPort());
		sandboxInitData.setClassFileRootPath(sandboxStartInfo.getProblemClassFileRootPath());

		Gson gson = new Gson();
		// 运行Jar文件，以此方式运行测评机，传递给测评机初始化参数
		String command = "java -jar " + sandboxStartInfo.getJarFilePath() + " " + gson.toJson(sandboxInitData);
		// 执行命令，返回命令执行结果
		Process process = Runtime.getRuntime().exec(command);
		return process;
	}

	/**
	 * 连接到新创建的测评机
	 * @param ip 测评机ip地址
	 * @param port 测评机端口号
	 * @param process 包含测评机进程ID的进程对象
	 * @return
	 */
	private String connectToNewMachine(String ip, int port, Process process) {
		Communicator communicator = new Communicator(ip, port, process);
		boolean flag = communicator.connectToMachine();
		if (!flag) {
			return null;
		}

		String url = ip + ":" + port;
		allCommunicators.put(url, communicator);
		noJudgingCommunicators.add(communicator);
		communicator.setEvaluationMachineHandler(new CommunicatorEvaluationMachineHandler(communicator, url));
		return url;
	}

	public void stopSingleCommunicatorById(String idCard) {
		Communicator javaCommunicator = allCommunicators.get(idCard);
		if (javaCommunicator == null) {
			throw new RuntimeException("没有该连接");
		}

		if (haveStopCommunicators.contains(javaCommunicator)) {
			return;
		}

		// 判断当前该连接是否为空闲连接
		if (noJudgingCommunicators.contains(javaCommunicator)) {
			// 因为集合本身是线程安全的，所以如果这里移除成功了，其它地方就不可能（不从map上拿的话）在拿到这个链接了。
			boolean remove = noJudgingCommunicators.remove(javaCommunicator);
			if (remove) {
				// 直接加入到停止集合里
				haveStopCommunicators.add(javaCommunicator);
				javaCommunicator.setStop(true);
				return;
			}
		}

		// 如果上面都不成立，只能设置为请求停止状态，等该沙箱处理完题目后，再将其停止
		javaCommunicator.setWantStop(true);
	}

	public void closeSandboxConnectById(String idCard) {
		Communicator javaCommunicator = allCommunicators.get(idCard);
		if (javaCommunicator == null) {
			throw new RuntimeException("没有该连接");
		}

		if (haveStopCommunicators.contains(javaCommunicator)) {
			// 直接关闭沙箱
			haveStopCommunicators.remove(javaCommunicator);
			allCommunicators.remove(idCard);
			javaCommunicator.closeWithMachineConnect();
			return;
		}

		// 判断当前该连接是否为空闲连接
		if (noJudgingCommunicators.contains(javaCommunicator)) {
			// 因为集合本身是线程安全的，所以如果这里移除成功了，那定时器那里，就不可能在拿到这个链接了。
			boolean remove = noJudgingCommunicators.remove(javaCommunicator);
			if (remove) {
				allCommunicators.remove(idCard);
				javaCommunicator.closeWithMachineConnect();
			}
		}

		// 如果上面都不成立，只能设置为请求关闭状态，等该沙箱处理完题目后，再将其停止
		javaCommunicator.setWantClose(true);
	}

	public void publicCommonRequest(String communicatorIdCard, CommonRequest commonRequest) {
		Communicator communicator = allCommunicators.get(communicatorIdCard);

		if (communicator != null) {
			communicator.sendRequest(commonRequest.getRequest(),
					commonRequest.getExecutor());
		}
	}

	/**
	 * 向判题请求阻塞队列中添加一个判题请求
	 * @param problemRequest 本次判题请求
	 */
	public void addEvaluationRequest(JudgeProblemRequest problemRequest) {
		problemRequests.add(problemRequest);
	}

	public CommunicatorStatus getCommunicatorStatus(String communicatorIdCard) {
		Communicator javaCommunicator = allCommunicators.get(communicatorIdCard);
		CommunicatorStatus c = new CommunicatorStatus();
		c.setJudgeing(javaCommunicator.isJudging());
		c.setStop(javaCommunicator.isStop());
		c.setWantClose(javaCommunicator.isWantClose());
		c.setWantStop(javaCommunicator.isWantStop());

		return c;
	}

	public int getPendingHandleProblemRequest() {
		return highPriorityProblemRequests.size() + problemRequests.size() + wantHandlePrblemCount;
	}

	/**
	 * 处理测评机状态请求
	 * 根据指定的测评机负责器{@code communicator}处理communicator
	 * 状态，和测评机注册信息
	 */
	private class CommunicatorEvaluationMachineHandler implements EvaluationMachineHandler {
		/**
		 * 需要处理的测评机
		 */
		private Communicator communicator;
		private String communicatorIdCard;

		public CommunicatorEvaluationMachineHandler(Communicator communicator, String communicatorIdCard) {
			this.communicator = communicator;
			this.communicatorIdCard = communicatorIdCard;
		}

		@Override
		public void handleIdleMachine() {
			if (communicator == null) {
				return;
			}
			// 从正在判题的阻塞队列中先移除出来
			judgingCommunicators.remove(communicator);
			communicator.setJudging(false);
			// 先判断，是否被设置了，想要停止工作的标志
			if (communicator.isWantStop()) {
				haveStopCommunicators.add(communicator);
				communicator.setStop(true);
				communicator.setWantStop(false);
			} else if (communicator.isWantClose()) {
				allCommunicators.remove(communicatorIdCard);
				communicator.closeWithMachineConnect();
			} else {
				noJudgingCommunicators.add(communicator);
				communicator.setStop(false);
				communicator.setWantStop(false);
			}
		}
	}

}

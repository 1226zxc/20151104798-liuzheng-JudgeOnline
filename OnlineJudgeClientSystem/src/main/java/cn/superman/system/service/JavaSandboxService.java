package cn.superman.system.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.util.ResourceUtils;

import cn.superman.constant.ConstantParameter;
import cn.superman.system.commandExecutor.ResponseExecutor;
import cn.superman.system.communicator.CommunicatorManager;
import cn.superman.system.dto.CommonRequest;
import cn.superman.system.dto.CommunicatorStatus;
import cn.superman.system.dto.JavaSandboxStartInfo;
import cn.superman.system.dto.JudgeProblemRequest;
import cn.superman.system.sandbox.constant.CommunicationSignal;
import cn.superman.system.sandbox.dto.Problem;
import cn.superman.system.sandbox.dto.ProblemResult;
import cn.superman.system.sandbox.dto.ProblemResultItem;
import cn.superman.system.sandbox.dto.Request;
import cn.superman.system.sandbox.dto.Response;
import cn.superman.system.sandbox.dto.SandBoxStatus;
import cn.superman.system.service.bean.SandboxStatus;
import cn.superman.system.service.dto.JudgeProblemDTO;
import cn.superman.system.service.dto.ProblemJudgeResult;
import cn.superman.system.service.dto.ProblemJudgeResultItem;
import cn.superman.system.service.observer.SandboxStatusObserver;
import cn.superman.system.util.JavaCompilerUtil;
import cn.superman.util.JsonUtil;
import cn.superman.util.Log4JUtil;
import cn.superman.web.exception.ServiceLogicException;
import cn.superman.web.util.ThreadFactoryUtil;

public class JavaSandboxService {
	private CommunicatorManager communicatorManager = null;
	private Map<String, SandboxStatus> sandboxStatusMap = new ConcurrentHashMap<String, SandboxStatus>();
	private static volatile JavaSandboxService javaSandboxService;
	private List<SandboxStatusObserver> sandboxStatusObservers = new CopyOnWriteArrayList<SandboxStatusObserver>();
	private ScheduledExecutorService statusTimer = Executors
			.newScheduledThreadPool(1, ThreadFactoryUtil
					.getLogThreadFactory(JavaSandboxService.class.getName()
							+ " statusTimer"));
	/**
	 * 执行判题任务的线程池
	 */
	private ExecutorService executorService = Executors.newCachedThreadPool(ThreadFactoryUtil
					.getLogThreadFactory(JavaSandboxService.class.getName() + " executorService"));

	public static JavaSandboxService getInstance() {
		if (javaSandboxService == null) {
			synchronized (JavaSandboxService.class) {
				if (javaSandboxService == null) {
					javaSandboxService = new JavaSandboxService();
				}
			}
		}

		return javaSandboxService;
	}

	private JavaSandboxService() {
		communicatorManager = CommunicatorManager.getInstance();
		openStatusListen();
	}

	private void openStatusListen() {
		// 每500毫秒，更新一次状态
		statusTimer.scheduleAtFixedRate(new Runnable() {
			private int count = 0;

			@Override
			public void run() {
				if (sandboxStatusMap.size() > 0) {
					Iterator<Entry<String, SandboxStatus>> iterator = sandboxStatusMap
							.entrySet().iterator();
					Entry<String, SandboxStatus> entry;
					while (iterator.hasNext()) {
						entry = iterator.next();
						// 相当于每5秒，通过网络向沙箱获取一次状态
						if (count == 10) {
							try {
								fillingSandboxStatusData(entry.getValue(), true);
							} catch (Exception e) {
								// 先简单处理，只要获取错误，就当做是这个沙箱线程意外的死亡了，直接做死亡处理
								Log4JUtil.logError(e);
								iterator.remove();
								communicatorManager
										.closeSandboxConnectById(entry
												.getValue().getIdCard());
							}
						} else {
							fillingSandboxStatusData(entry.getValue(), false);
						}
					}
					count++;
					count %= 11;
				}

				notifyAllStatusObserver(sandboxStatusMap.values());
			}
		}, 0, 500, TimeUnit.MILLISECONDS);
	}

	private void notifyAllStatusObserver(Collection<SandboxStatus> status) {
		for (SandboxStatusObserver observer : sandboxStatusObservers) {
			observer.statusChanged(status);
		}
	}

	public static void main(String[] args) throws IOException {
		new JavaSandboxService().openNewJavaSandbox();
	}

	/**
	 * 开启新的测评机。
	 * 采用多线程方式开启测评机。指定了开启测评机的处理规则，
	 * 将此规则提交给线程池去运行开启
	 */
	@SuppressWarnings("Duplicates")
	public void openNewJavaSandbox() {
		executorService.submit(new Runnable() {
			@Override
			public void run() {

				String ip = "127.0.0.1";
				int port = getValidport();
				// 设置测评机初始化信息（IP地址、端口号）
				JavaSandboxStartInfo sandboxStartInfo = new JavaSandboxStartInfo();
				sandboxStartInfo.setIp(ip);
				sandboxStartInfo.setPort(port);
				// 设置class文件的路径，以便测评机加载。
				// 字符串前面和后面不多添加一个"的话，沙箱那边收到的json数据有问题
				sandboxStartInfo.setProblemClassFileRootPath("\"" + ConstantParameter.CLASS_FILE_ROOT_PATH + "\"");
				try {
					sandboxStartInfo.setJarFilePath(ResourceUtils.getFile(ResourceUtils
							.CLASSPATH_URL_PREFIX + "sandbox/EvaluationMachine.jar").getAbsolutePath());
				} catch (FileNotFoundException e) {
					Log4JUtil.logError(e);
					return;
				}

				String sandboxIdCard = communicatorManager.makeNewSandBox(sandboxStartInfo);
				if (sandboxIdCard == null) {
					throw new ServiceLogicException("创建失败");
				}

				SandboxStatus sandboxStatus = new SandboxStatus(sandboxIdCard, ip, port);
				sandboxStatusMap.put(sandboxIdCard, sandboxStatus);
				fillingSandboxStatusData(sandboxStatus, true);
			}
		});

		executorService.execute(new Runnable() {

			@Override
			public void run() {
				String ip = "127.0.0.1";
				int port = getValidport();
				JavaSandboxStartInfo sandboxStartInfo = new JavaSandboxStartInfo();
				sandboxStartInfo.setIp(ip);
				sandboxStartInfo.setPort(port);
				// 字符串前面和后面不多添加一个"的话，沙箱那边收到的json数据有问题
				sandboxStartInfo.setProblemClassFileRootPath("\""
						+ ConstantParameter.CLASS_FILE_ROOT_PATH + "\"");
				try {
					sandboxStartInfo.setJarFilePath(ResourceUtils.getFile(
							ResourceUtils.CLASSPATH_URL_PREFIX
									+ "sandbox/EvaluationMachine.jar")
							.getAbsolutePath());
				} catch (FileNotFoundException e) {
					Log4JUtil.logError(e);
					return;
				}

				String sandboxIdCard = communicatorManager
						.makeNewSandBox(sandboxStartInfo);
				if (sandboxIdCard == null) {
					throw new ServiceLogicException("创建失败");
				}

				SandboxStatus sandboxStatus = new SandboxStatus(sandboxIdCard,
						ip, port);
				sandboxStatusMap.put(sandboxIdCard, sandboxStatus);
				fillingSandboxStatusData(sandboxStatus, true);
			}
		});

	}

	/**
	 * @param
	 * @param getFullData
	 *            如果为true的话，这个方法还会向沙箱发出一个请求,远程获得该沙箱的一些信息（比如，当前使用内存，pid等）
	 */
	private void fillingSandboxStatusData(SandboxStatus sandboxStatus,
			boolean getFullData) {
		if (getFullData) {
			CommonRequest commonRequest = new CommonRequest();
			commonRequest.setExecutor(new SandboxStatusResponseExecutor(
					sandboxStatus));
			Request request = new Request();
			request.setCommand(CommunicationSignal.RequestSignal.SANDBOX_STATUS);
			commonRequest.setRequest(request);
			communicatorManager.publicCommonRequest(sandboxStatus.getIdCard(),
					commonRequest);
		}

		CommunicatorStatus communicatorStatus = communicatorManager
				.getCommunicatorStatus(sandboxStatus.getIdCard());
		sandboxStatus.setJudgeing(communicatorStatus.isJudgeing());
		sandboxStatus.setWantClose(communicatorStatus.isWantClose());
		sandboxStatus.setWantStop(communicatorStatus.isWantStop());
		sandboxStatus.setRunning(!communicatorStatus.isStop());
	}

	/**
	 * 提交判题任务，多线程处理。处理步骤为
	 * - 编译java源文件
	 * - 设置时间限制、内存限制、源代码路径、测试用例等判题任务信息，以便传递测评机
	 * - 设置本次请求测评机的实体类信息，包括测评机的行为指令
	 * @param judgeProblemDTO 保存着时间限制、内存限制、源代码路径、测试用例
	 * @param errorListener
	 */
	public void commitJudgementRequest(final JudgeProblemDTO judgeProblemDTO, final ErrorListener errorListener) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 编译java文件
					boolean flag = JavaCompilerUtil.compilerJavaFile(judgeProblemDTO.getJavaFilePath(),
							ConstantParameter.CLASS_FILE_ROOT_PATH);

					if (!flag) {
						if (errorListener != null) {
							errorListener.onError(new ServiceLogicException("编译失败"));
						}
						return;
					}

					JudgeProblemRequest problemRequest = new JudgeProblemRequest();
					problemRequest.setExecutor(new ProblemResponseExecutor(judgeProblemDTO.getProblemOutputPathList(),
							judgeProblemDTO.getJudgeResultListener()));

					// 创建请求测评机实体类
					Request request = new Request();
					// 设置判题请求指令，后台测评机将会进行判题
					request.setCommand(CommunicationSignal.RequestSignal.REQUSET_JUDGED_PROBLEM);

					Problem problem = new Problem();
					problem.setTimeLimit(judgeProblemDTO.getTimeLimit());
					problem.setRunId(judgeProblemDTO.getRunId());
					// 数据库记录的是KB，但是测评机那边要的是B，所以这里要转换一下单位
					problem.setMemoryLimit(judgeProblemDTO.getMemoryLimit() * 1024);
					problem.setInputDataFilePathList(judgeProblemDTO.getProblemInputPathList());
					// 截取文件名
					problem.setClassFileName(judgeProblemDTO.getJavaFilePath().substring(
									judgeProblemDTO.getJavaFilePath().lastIndexOf(File.separator) + 1,
									judgeProblemDTO.getJavaFilePath().lastIndexOf(".")));

					request.setData(JsonUtil.toJson(problem));
					problemRequest.setRequest(request);

					// 添加判题等待队列 BlockingQueue
					communicatorManager.addJudgeProblemRequest(problemRequest);
				} catch (Exception e) {
					if (errorListener != null) {
						errorListener.onError(e);
					}
				}

			}
		});

	}

	private void processJudgeResult(Response response, List<String> problemOutputPathList,
			JudgeResultListener judgeResultListener) {
		ProblemJudgeResult problemJudgeResult = new ProblemJudgeResult();
		List<ProblemJudgeResultItem> problemJudgeResultItems = new ArrayList<ProblemJudgeResultItem>();
		ProblemResult problemResult = JsonUtil.toBean(response.getData(), ProblemResult.class);
		List<ProblemResultItem> resultItems = problemResult.getResultItems();

		if (resultItems == null || resultItems.size() == 0) {
			if (judgeResultListener != null) {
				problemJudgeResult.setCorrectRate(0);
				problemJudgeResult.setProblemJudgeResultItems(Collections.<ProblemJudgeResultItem> emptyList());
				judgeResultListener.judgeResult(problemJudgeResult);
				return;
			}
		}

		// 准备好输出文件信息，用于与输入文件匹配,用文件名匹配，因为匹配的输入输出文件名是一致的
		Map<String, String> outputFilesMap = new HashMap<String, String>();
		String outputFilePath = null;
		for (int i = 0; i < problemOutputPathList.size(); i++) {
			outputFilePath = problemOutputPathList.get(i);
			// input1.txt -> E:\\input1.txt
			outputFilesMap.put(outputFilePath.substring(outputFilePath.lastIndexOf(File.separator) + 1), outputFilePath);
		}

		int correctProblemCount = 0;
		ProblemJudgeResultItem judgeResultItem = null;
		String itemOutputFilePath = null;

		// 遍历从后台测评机每一个测试用例的测评结果
		for (ProblemResultItem resultItem : resultItems) {
			// 没有测试用例文件
			if (resultItem == null || resultItem.getInputFilePath() == null) {
				continue;
			}

			judgeResultItem = new ProblemJudgeResultItem();
			// 根据输入文件和输出文件约定的规则，获取相应的输出文件
			outputFilePath = resultItem.getInputFilePath();
			itemOutputFilePath = outputFilesMap.get(outputFilePath.substring(
					outputFilePath.lastIndexOf(File.separator) + 1));

			// 代码是否无异常运行
			if (resultItem.isNormal()) {
				boolean isRight = checkResultIsRightORNot(itemOutputFilePath, resultItem.getResult());
				if (isRight) {
					judgeResultItem.setMessage("答案正确");
					judgeResultItem.setRight(true);
					correctProblemCount++;
				} else {
					judgeResultItem.setMessage("答案错误");
				}
			} else {
				// 这些是还没判断结果，就已经有问题的
				judgeResultItem.setMessage(resultItem.getMessage());
			}

			judgeResultItem.setUseTime(resultItem.getUseTime());
			// 因为那边返回的是B，这里要转换为KB
			judgeResultItem.setUseMemory(resultItem.getUseMemory() / 1024);
			judgeResultItem.setInputFilePath(resultItem.getInputFilePath());
			judgeResultItem.setOutputFilePath(itemOutputFilePath);
			problemJudgeResultItems.add(judgeResultItem);
		}

		problemJudgeResult.setRunId(problemResult.getRunId());
		problemJudgeResult.setCorrectRate((float) correctProblemCount / resultItems.size());
		problemJudgeResult.setProblemJudgeResultItems(problemJudgeResultItems);

		if (judgeResultListener != null) {
			judgeResultListener.judgeResult(problemJudgeResult);
		}
	}

	/**
	 * 检测运行结果是否正确
	 * 与标准结果输出文件比较
	 * @param standardResultFilePath 标准结果输出文件
	 * @param beTestedResult 代码实际运行的结果
	 * @return
	 */
	private boolean checkResultIsRightORNot(String standardResultFilePath, String beTestedResult) {
		FileInputStream inputStream = null;
		Scanner scanner = null;
		StringBuilder builder = new StringBuilder();

		try {
			inputStream = new FileInputStream(standardResultFilePath);
			scanner = new Scanner(inputStream);

			while (scanner.hasNextLine()) {
				builder.append(scanner.nextLine() + "\r\n");
			}
			return builder.toString().equals(beTestedResult);
		} catch (Exception e) {
			throw new RuntimeException("匹配失败" + e.getMessage());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (scanner != null) {
					scanner.close();
				}
			} catch (IOException e) {

			}
		}
	}

	public void closeSandboxById(final String idCard) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				sandboxStatusMap.remove(idCard);
				CommonRequest commonRequest = new CommonRequest();
				commonRequest.setExecutor(new CloseSandboxResponseExecutor(
						idCard));
				Request request = new Request();
				request.setCommand(CommunicationSignal.RequestSignal.CLOSE_SANDBOX);
				commonRequest.setRequest(request);
				communicatorManager.publicCommonRequest(idCard, commonRequest);
			}
		});

	}

	public void closeAllSandbox() {
		for (Map.Entry<String, SandboxStatus> entry : sandboxStatusMap
				.entrySet()) {
			closeSandboxById(entry.getValue().getIdCard());
		}
	}

	private void closeSandboxConnectById(String idCard) {
		communicatorManager.closeSandboxConnectById(idCard);
	}

	public void addSandboxStatusObserver(SandboxStatusObserver o) {
		sandboxStatusObservers.add(o);
	}

	public void removeSandboxStatusObserver(SandboxStatusObserver o) {
		sandboxStatusObservers.remove(o);
	}

	private static int portIndex = 10;
	private static int basePortIndex = 60000;

	private synchronized int getValidport() {
		if (portIndex >= 5535) {
			basePortIndex -= 10000;
			portIndex = 10;
		}
		portIndex++;
		return basePortIndex + portIndex;
	}

	public int getPendingHandleProblemCount() {
		return communicatorManager.getPendingHandleProblemRequest();
	}

	public interface JudgeResultListener {
		void judgeResult(ProblemJudgeResult problemJudgeResult);
	}

	private class ProblemResponseExecutor implements ResponseExecutor {
		private List<String> problemOutputPathList;
		private JudgeResultListener judgeResultListener;

		public ProblemResponseExecutor(List<String> problemOutputPathList, JudgeResultListener judgeResultListener) {
			this.problemOutputPathList = problemOutputPathList;
			this.judgeResultListener = judgeResultListener;
		}

		@Override
		public void executor(Response response) {
			processJudgeResult(response, problemOutputPathList, judgeResultListener);
		}
	}

	private class CloseSandboxResponseExecutor implements ResponseExecutor {
		private String sandboxIdCard;

		public CloseSandboxResponseExecutor(String sandboxIdCard) {
			this.sandboxIdCard = sandboxIdCard;
		}

		@Override
		public void executor(Response response) {
			if (CommunicationSignal.ResponseSignal.OK.equals(response
					.getResponseCommand())) {
				closeSandboxConnectById(sandboxIdCard);
			}
		}
	}

	private static class SandboxStatusResponseExecutor implements
			ResponseExecutor {
		private SandboxStatus sandboxStatus;

		public SandboxStatusResponseExecutor(SandboxStatus sandboxStatus) {
			this.sandboxStatus = sandboxStatus;
		}

		@Override
		public void executor(Response response) {
			SandBoxStatus status = JsonUtil.toBean(response.getData(),
					SandBoxStatus.class);
			sandboxStatus.setBeginTime(status.getBeginStartTime());
			sandboxStatus.setPid(status.getPid());
			sandboxStatus.setUseMemory(status.getUseMemory());
			sandboxStatus = null;
		}

	}

	public static interface ErrorListener {
		void onError(Exception exception);
	}
}

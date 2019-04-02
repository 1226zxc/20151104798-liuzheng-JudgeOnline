package com.lz.system.communicator.messageProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.lz.system.commandExecutor.ResponseExecutor;
import com.lz.system.communicator.listener.JudgementMachineHandler;
import com.lz.system.sandbox.constant.CommunicationSignal;
import com.lz.system.sandbox.dto.Request;
import com.lz.system.sandbox.dto.Response;
import com.lz.util.JsonUtil;
import com.lz.util.Log4JUtil;
import com.lz.util.UUIDUtil;

/**
 * 消息处理器
 * 消息处理器包含核心方法sendRequest()，可以向后台测评机发送请求数据
 */
public class MessageProcessor {
	private static Map<String, ResponseExecutor> responseExecutors = new HashMap<String, ResponseExecutor>();
	private Thread messageBoxThread;

	/**
	 * Socket 通信输入流
	 */
	private Scanner scanner;

	/**
	 * Socket 通信输出流
	 */
	private OutputStream outputStream;

	/**
	 *  测评机空闲状态处理器
	 */
	private JudgementMachineHandler idleListener;

	public MessageProcessor(InputStream inputStream, OutputStream outputStream) {
		try {
			scanner = new Scanner(new InputStreamReader(inputStream, "UTF-8"));
			this.outputStream = outputStream;
			listenResponse();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 消息监听器
	 * 线程轮询查询监听来自后台测评机响应的信息
	 */
	@SuppressWarnings("all")
	private void listenResponse() {
		messageBoxThread = new Thread() {
			@Override
			public void run() {
				String message = null;
				Response response = null;

				while (!this.isInterrupted()) {
					if (!scanner.hasNextLine()) {
						continue;
					}
					// 监听到Socet有响应信息，根据响应信号响应信息
					message = scanner.nextLine();
					response = JsonUtil.toBean(message, Response.class);
					if (CommunicationSignal.ResponseSignal.IDLE.equals(response.getResponseCommand())) {
						if (idleListener != null) {
							idleListener.sandBoxIdleNow();
						}
						// 这里理应再修改一下，利用观察者模式，做到对修改关闭，做扩展开放，可以动态添加多个监听器
					} else if (CommunicationSignal.ResponseSignal.ERROR.equals(response.getResponseCommand())) {
						Log4JUtil.logError(new RuntimeException(response.getData()));
					} else {
						ResponseExecutor commandExecutor = responseExecutors.remove(response.getSignalId());
						if (commandExecutor != null) {
							commandExecutor.executor(response);
						}
					}
				}
			}
		};

		messageBoxThread.start();
		messageBoxThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				Log4JUtil.logError(e);
			}
		});
	}

	public void close() {
		try {
			outputStream.close();
		} catch (IOException e) {
			Log4JUtil.logError(e);
		}
		scanner.close();
		messageBoxThread.interrupt();
	}

	/**
	 * 将请求通过Socket发送到后台测评机
	 * @param request
	 * @param executor
	 */
	public void sendRequset(Request request, ResponseExecutor executor) {
		try {
			request.setSignalId(UUIDUtil.getUUID());
			String data = JsonUtil.toJson(request);
			addExecutor(request.getSignalId(), executor);
			// 此时的输出流是Socket输出流，意味着向网络写入数据
			outputStream.write((data + "\n").getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void addExecutor(String signalId, ResponseExecutor commandExecutor) {
		responseExecutors.put(signalId, commandExecutor);
	}

	public JudgementMachineHandler getIdleListener() {
		return idleListener;
	}

	public void setIdleListener(JudgementMachineHandler idleListener) {
		this.idleListener = idleListener;
	}

}

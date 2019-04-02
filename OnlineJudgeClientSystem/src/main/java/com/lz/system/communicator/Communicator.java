package com.lz.system.communicator;

import java.io.IOException;
import java.net.Socket;

import com.lz.system.commandExecutor.ResponseExecutor;
import com.lz.system.communicator.listener.EvaluationMachineHandler;
import com.lz.system.communicator.messageProcessor.MessageProcessor;
import com.lz.system.sandbox.dto.Request;
import com.lz.util.Log4JUtil;

/**
 * 与后台测评机沟通的交流器，测评机的状态将由此类为委托描述
 * 交流器实例对象负责与一个测评机线程通信，交流器与测评机是
 * 绑定在一起的。再测评机开启时，调用connectToSandbox()连接
 * 指定测评机线程。连接成功后可以使用方法sendRequset()向测评机
 * 发送请求数据。
 *
 * @author 刘铮
 */
public class Communicator {
	private Socket socket;
	private Process process;
	private String ip;
	private int port;

	/**
	 * 每一个负责与测评机收发信息的Communicator实例实质都是
	 * 下面的实例在干活
	 */
	private MessageProcessor messageProcessor;

	/**
	 * 与此连接的测评机机是否正在判题
	 */
	private boolean isJudging;
	private boolean isWantStop;
	private boolean isStop;
	private boolean isWantClose;

	public Communicator() {

	}

	public Communicator(String ip, int port, Process process) {
		this.ip = ip;
		this.port = port;
		this.process = process;
	}

	/**
	 * 连接测评机。并设置socket输入流输出流以便通信
	 *
	 * @return
	 */
	public boolean connectToMachine() {
		if (socket == null) {
			try {
				socket = new Socket(ip,port);
				//socket.connect(new InetSocketAddress(ip, port));
				messageProcessor = new MessageProcessor(socket.getInputStream(), socket.getOutputStream());
			} catch (Exception e) {
				Log4JUtil.logError(e);
				return false;
			}
		}

		return true;
	}

	public void closeWithMachineConnect() {
		if (socket != null) {
			try {
				messageProcessor.close();
				messageProcessor = null;
				socket.close();
				socket = null;
				if (process.isAlive()) {
					process.destroyForcibly();
				}
			} catch (IOException e) {
				Log4JUtil.logError(e);
			}
		}
	}

	/**
	 * 向测评机发送请求数据
	 *
	 * @param request 请求数据
	 * @param executor
	 */
	public void sendRequest(Request request, ResponseExecutor executor) {
		messageProcessor.transmitRequest(request, executor);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isWantStop() {
		return isWantStop;
	}

	public void setWantStop(boolean isWantStop) {
		this.isWantStop = isWantStop;
	}

	public boolean isStop() {
		return isStop;
	}

	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}

	public void setEvaluationMachineHandler(EvaluationMachineHandler idleListener) {
		if (messageProcessor != null) {
			messageProcessor.setMachineHandler(idleListener);
		}
	}

	public boolean isWantClose() {
		return isWantClose;
	}

	public void setWantClose(boolean isWantClose) {
		this.isWantClose = isWantClose;
	}

	public boolean isJudging() {
		return isJudging;
	}

	public void setJudging(boolean isJudgeing) {
		this.isJudging = isJudgeing;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Communicator other = (Communicator) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

}

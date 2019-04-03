package com.lz.machine.dto;

/**
 * 保存的是从前台发送过来的数据，包括
 * 对测评机的操作的指令和数据代码
 */
public class Request {
	/**
	 * 请求测评机的指令，该指令用于操作测评机
	 */
	private String command;

	/**
	 * 保存了提交的JSON格式的代码
	 */
	private String task;
	private String requestId;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

}

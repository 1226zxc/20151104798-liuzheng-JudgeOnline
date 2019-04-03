package com.lz.system.sandbox.dto;

/**
 * 测评机请求实体类，保存
 * - 对测评机操作指令
 * - 时间限制、内存限制、class类路径、测试用例输入输出文件路径的Json格式data属性
 * 这个实体类将决定测评机的行为和任务
 *
 * @author 刘铮
 */
public class Request {
	/**
	 * 操作测评机的指令
	 */
	private String command;

	/**
	 * 测评机使用的请求数据
	 */
	private String task;

	/**
	 * 与测评机通信的id，每一个实例此处id都是不同的
	 */
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

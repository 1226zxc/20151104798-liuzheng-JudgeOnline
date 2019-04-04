package com.lz.system.sandbox.dto;

/**
 * 存储测评机响应给前台的信息
 *
 * @author 刘铮
 */
public class Response {
	/**
	 * 本次请求指令
	 */
	private String requestCommand;

	/**
	 * 响应指令
	 */
	private String responseCommand;

	/**
	 * 响应数据
	 */
	private String data;

	/**
	 *
	 */
	private String requestId;

	public String getResponseCommand() {
		return responseCommand;
	}

	public void setResponseCommand(String responseCommand) {
		this.responseCommand = responseCommand;
	}

	public String getRequestCommand() {
		return requestCommand;
	}

	public void setRequestCommand(String requestCommand) {
		this.requestCommand = requestCommand;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public String toString() {
		return "Response [requestCommand=" + requestCommand
				+ ", responseCommand=" + responseCommand + ", data=" + data
				+ ", requestId=" + requestId + "]";
	}

}

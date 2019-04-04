package com.lz.machine.dto;

/**
 * 响应给前台信息的实体类
 *
 * @author 刘铮
 */
public class Response {
	private String requestCommand;
	private String responseCommand;
	private String data;
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

}

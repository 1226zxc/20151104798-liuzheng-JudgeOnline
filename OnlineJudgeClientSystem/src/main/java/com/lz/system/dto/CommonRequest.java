package com.lz.system.dto;

import com.lz.system.commandExecutor.ResponseExecutor;
import com.lz.system.sandbox.dto.Request;

public class CommonRequest {
	/**
	 * 响应结果处理器
	 */
	private ResponseExecutor executor;

	/**
	 * 请求实体类
	 */
	private Request request;

	public ResponseExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(ResponseExecutor executor) {
		this.executor = executor;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
}

package com.lz.system.commandExecutor;

import com.lz.system.sandbox.dto.Response;

/**
 * 响应执行器
 * 执行来自测评机的响应结果
 *
 * @author 刘铮
 */
public interface ResponseExecutor {
	/**
	 * 执行响应结果
	 * @param response 需要执行的响应结果
	 */
	void execute(Response response);
}

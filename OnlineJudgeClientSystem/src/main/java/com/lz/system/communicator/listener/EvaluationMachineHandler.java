package com.lz.system.communicator.listener;

/**
 * 对测评机操作的处理器
 *
 * @author 刘铮
 */
public interface EvaluationMachineHandler {

	/**
	 * 处理已经闲置状态的测评机
	 */
	void handleIdleMachine();
}

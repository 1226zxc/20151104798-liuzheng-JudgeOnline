package com.lz.system.sandbox.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 问题描述实体类。描述判题任务的信息：
 * - 时间限制
 * - 内存限制
 * - class文件名
 * - 本次运行id
 * - 测试用例输入文件路径
 * 此类用于与测评机通信，前台与测评机之间传递
 * 的格式即是此类
 *
 * @author 刘铮
 */
public class Problem {
	private long timeLimit;
	private long memoryLimit;
	private String classFileName;
	private String runId;
	private List<String> inputDataFilePathList = new ArrayList<String>();

	public long getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(long timeLimit) {
		this.timeLimit = timeLimit;
	}

	public long getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(long memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	public String getClassFileName() {
		return classFileName;
	}

	public void setClassFileName(String classFileName) {
		this.classFileName = classFileName;
	}

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public List<String> getInputDataFilePathList() {
		return inputDataFilePathList;
	}

	public void setInputDataFilePathList(List<String> inputDataFilePathList) {
		this.inputDataFilePathList = inputDataFilePathList;
	}

}

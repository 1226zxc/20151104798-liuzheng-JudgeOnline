package com.lz.machine.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存这个问题的相关内容
 * 有时间限制，内存限制、类加载路径
 * 运行id和输入数据文件路径
 */
public class Task {
	private long timeLimit;
	private long memoryLimit;
	/**
	 * 保存的是待测验的class文件名
	 */
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

	@Override
	public String toString() {
		return "Task [timeLimit=" + timeLimit + ", memoryLimit="
				+ memoryLimit + ", classFileName=" + classFileName + ", runId="
				+ runId + ", inputDataFilePathList=" + inputDataFilePathList
				+ "]";
	}

}

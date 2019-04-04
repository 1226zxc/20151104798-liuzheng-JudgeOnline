package com.lz.machine.dto;

/**
 * 根据一个测试用例的代码测评结果
 * 包括使用时间、使用内存量、结果、信息等等
 */
public class TaskItemResult {
	private long useTime;
	private long useMemory;

	/**
	 * 程序运行的结果
	 */
	private String result;

	/**
	 * 如果程序运行出错，这个属性将保存错误信息
	 */
	private String message;

	/**
	 * 程序是否正常运行（指的是运行无异常，不考虑结果正误）
	 */
	private boolean isNormal;

	private String inputFilePath;

	public long getUseTime() {
		return useTime;
	}

	public void setUseTime(long useTime) {
		this.useTime = useTime;
	}

	public long getUseMemory() {
		return useMemory;
	}

	public void setUseMemory(long useMemory) {
		this.useMemory = useMemory;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isNormal() {
		return isNormal;
	}

	public void setNormal(boolean isNormal) {
		this.isNormal = isNormal;
	}

	public String getInputFilePath() {
		return inputFilePath;
	}

	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}

	@Override
	public String toString() {
		return "TaskItemResult [useTime=" + useTime + ", useMemory="
				+ useMemory + ", result=" + result + ", message=" + message
				+ ", isNormal=" + isNormal + ", inputFilePath=" + inputFilePath
				+ "]";
	}

}

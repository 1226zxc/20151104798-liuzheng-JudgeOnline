package com.lz.system.service.dto;

import java.util.List;

import com.lz.system.service.EvaluationMachineService;

/**
 * 判题任务实体类
 *
 * @author 刘铮
 */
public class JudgeProblemDTO {
	private String runId;

	/**
	 * 提交的保存到本地的java源文件路径
	 */
	private String sourceCodeFilePath;

	/**
	 * 时间限制
	 */
	private long timeLimit;

	/**
	 * 内存限制
	 */
	private long memoryLimit;

	/**
	 * 测试用例输入文件路径，作为标准输入流
	 */
	private List<String> problemInputPathList;

	/**
	 * 测试用例输出文件路径，作为标准输出流
	 */
	private List<String> problemOutputPathList;

	private EvaluationMachineService.EvaluationResultHandler evaluationResultHandler;

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public String getSourceCodeFilePath() {
		return sourceCodeFilePath;
	}

	public void setSourceCodeFilePath(String sourceCodeFilePath) {
		this.sourceCodeFilePath = sourceCodeFilePath;
	}

	public List<String> getProblemInputPathList() {
		return problemInputPathList;
	}

	public void setProblemInputPathList(List<String> problemInputPathList) {
		this.problemInputPathList = problemInputPathList;
	}

	public List<String> getProblemOutputPathList() {
		return problemOutputPathList;
	}

	public void setProblemOutputPathList(List<String> problemOutputPathList) {
		this.problemOutputPathList = problemOutputPathList;
	}

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

	public EvaluationMachineService.EvaluationResultHandler getEvaluationResultHandler() {
		return evaluationResultHandler;
	}

	public void setEvaluationResultHandler(EvaluationMachineService.EvaluationResultHandler evaluationResultHandler) {
		this.evaluationResultHandler = evaluationResultHandler;
	}

	@Override
	public String toString() {
		return "JudgeProblemDTO{" +
				"runId='" + runId + '\'' +
				", sourceCodeFilePath='" + sourceCodeFilePath + '\'' +
				", timeLimit=" + timeLimit +
				", memoryLimit=" + memoryLimit +
				", problemInputPathList=" + problemInputPathList +
				", problemOutputPathList=" + problemOutputPathList +
				", evaluationResultHandler=" + evaluationResultHandler +
				'}';
	}
}

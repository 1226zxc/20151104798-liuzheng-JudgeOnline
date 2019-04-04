package com.lz.machine.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存的是运行时id和保存所有结果项的list
 */
public class TaskResult {
	private String runId;
	private List<TaskItemResult> resultItems = new ArrayList<TaskItemResult>();

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public List<TaskItemResult> getResultItems() {
		return resultItems;
	}

	public void setResultItems(List<TaskItemResult> resultItems) {
		this.resultItems = resultItems;
	}

}

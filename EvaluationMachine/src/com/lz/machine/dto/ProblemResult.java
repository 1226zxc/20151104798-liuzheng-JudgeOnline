package com.lz.machine.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存的是运行时id和保存所有结果项的list
 */
public class ProblemResult {
	private String runId;
	private List<ProblemResultItem> resultItems = new ArrayList<ProblemResultItem>();

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public List<ProblemResultItem> getResultItems() {
		return resultItems;
	}

	public void setResultItems(List<ProblemResultItem> resultItems) {
		this.resultItems = resultItems;
	}

}

package com.lz.web.service.admin;

import com.lz.system.service.EvaluationMachineService;
import com.lz.system.service.observer.SandboxStatusObserver;
import com.lz.util.AppSystemInfoUtil;
import com.lz.util.OperationSystemInfoUtil;
import org.springframework.stereotype.Service;

@Service
public class AdminOJSystemService {
	private EvaluationMachineService evaluationMachineService;

	public AdminOJSystemService() {
		evaluationMachineService = EvaluationMachineService.getInstance();
	}

	public void openNewJavaSandbox() {
		evaluationMachineService.openNewJavaSandbox();
	}

	public void addJavaSandboxStatusListen(SandboxStatusObserver o) {
		evaluationMachineService.addSandboxStatusObserver(o);
	}

	public void removeJavaSandboxStatusListen(SandboxStatusObserver o) {
		evaluationMachineService.removeSandboxStatusObserver(o);
	}

	public void closeAllJavaSandbox() {
		evaluationMachineService.closeAllSandbox();
	}

	public void closeSandboxById(String idCard) {
		evaluationMachineService.closeSandboxById(idCard);
	}

	public int getPendingHandleProblemRequest() {
		return evaluationMachineService.getPendingHandleProblemCount();
	}

	public OperationSystemInfoUtil.OperationSystemInfo getOperationSystemInfo() {
		return OperationSystemInfoUtil.getOperationSystemInfo();
	}

	public AppSystemInfoUtil.AppInfo getAppInfo() {
		return AppSystemInfoUtil.getAppInfo();
	}
}

package com.lz.web.service.admin;

import com.lz.system.service.JavaSandboxService;
import com.lz.system.service.observer.SandboxStatusObserver;
import com.lz.util.AppSystemInfoUtil;
import com.lz.util.OperationSystemInfoUtil;
import org.springframework.stereotype.Service;

@Service
public class AdminOJSystemService {
	private JavaSandboxService javaSandboxService;

	public AdminOJSystemService() {
		javaSandboxService = JavaSandboxService.getInstance();
	}

	public void openNewJavaSandbox() {
		javaSandboxService.openNewJavaSandbox();
	}

	public void addJavaSandboxStatusListen(SandboxStatusObserver o) {
		javaSandboxService.addSandboxStatusObserver(o);
	}

	public void removeJavaSandboxStatusListen(SandboxStatusObserver o) {
		javaSandboxService.removeSandboxStatusObserver(o);
	}

	public void closeAllJavaSandbox() {
		javaSandboxService.closeAllSandbox();
	}

	public void closeSandboxById(String idCard) {
		javaSandboxService.closeSandboxById(idCard);
	}

	public int getPendingHandleProblemRequest() {
		return javaSandboxService.getPendingHandleProblemCount();
	}

	public OperationSystemInfoUtil.OperationSystemInfo getOperationSystemInfo() {
		return OperationSystemInfoUtil.getOperationSystemInfo();
	}

	public AppSystemInfoUtil.AppInfo getAppInfo() {
		return AppSystemInfoUtil.getAppInfo();
	}
}

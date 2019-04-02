package com.lz.system.service.observer;

import java.util.Collection;

import com.lz.system.service.bean.SandboxStatus;

public interface SandboxStatusObserver {
	void statusChanged(Collection<SandboxStatus> status);
}

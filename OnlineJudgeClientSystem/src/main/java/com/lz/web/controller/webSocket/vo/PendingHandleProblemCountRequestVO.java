package com.lz.web.controller.webSocket.vo;

public class PendingHandleProblemCountRequestVO extends WebSocketRequestVO {
	private boolean isReceivePendingHandleProblemCount;

	public boolean isReceivePendingHandleProblemCount() {
		return isReceivePendingHandleProblemCount;
	}

	public void setReceivePendingHandleProblemCount(
			boolean isReceivePendingHandleProblemCount) {
		this.isReceivePendingHandleProblemCount = isReceivePendingHandleProblemCount;
	}

}

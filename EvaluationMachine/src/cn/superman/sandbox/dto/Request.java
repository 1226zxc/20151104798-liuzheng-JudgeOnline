package cn.superman.sandbox.dto;

/**
 * 保存的是从前台发送过来的数据，包括
 * 对测评机的操作的指令和数据代码
 */
public class Request {
	/**
	 * 请求测评机的指令，该指令用于操作测评机
	 */
	private String command;

	/**
	 * 保存了提交的JSON格式的代码
	 */
	private String data;
	private String signalId;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getSignalId() {
		return signalId;
	}

	public void setSignalId(String signalId) {
		this.signalId = signalId;
	}

}

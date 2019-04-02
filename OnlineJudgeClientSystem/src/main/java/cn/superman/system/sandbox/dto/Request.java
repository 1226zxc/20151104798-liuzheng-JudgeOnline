package cn.superman.system.sandbox.dto;

/**
 * 测评机请求实体类，保存
 * - 对测评机操作指令
 * - 时间限制、内存限制、class类路径、测试用例输入输出文件路径的Json格式data属性
 * 这个实体类将决定测评机的行为和任务
 *
 * @author 刘铮
 */
public class Request {
	/**
	 * 操作测评机的指令
	 */
	private String command;

	/**
	 * 测评机使用的请求数据
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

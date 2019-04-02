package cn.superman.web.constant;

/**
 * 邮箱枚举
 * 包含邮箱主机地址、用户名和密码
 * @author 刘铮
 */
public enum MailConstant {
	userName("1151063116@qq.com"), password("liuzheng1226.!"),
	host("smtp.qq.com");
	private String value;

	private MailConstant(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}

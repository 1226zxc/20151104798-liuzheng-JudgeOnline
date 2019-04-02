package com.lz.web.util.mail;

import java.util.ArrayList;
import java.util.List;

/**
 * 邮件类
 * 可以设置发件人，收件人，主题，文本内容，附加文字，抄送，暗送
 * 
 * @author 刘铮
 */
public class Mail {
	/**
	 * 发件人
	 */
	private String fromAddress;

	/**
	 * 收件人，用“，”分割
	 */
	private StringBuilder toAddress = new StringBuilder();

	/**
	 * 抄送
	 */
	private StringBuilder ccAddress = new StringBuilder();

	/**
	 * 暗送
	 */
	private StringBuilder bccAddress = new StringBuilder();

	/**
	 * 主题
	 */
	private String subject;

	/**
	 * 正文
	 */
	private String content;

	/**
	 * 附件列表
	 */
	private List<AttachBean> attachList = null;

	public Mail(String fromAddress, String toAddress) {
		this.fromAddress = fromAddress;
		addToAddress(toAddress);
	}

	public Mail(String fromAddress, String toAddress, String subject,
			String content) {
		this.fromAddress = fromAddress;
		addToAddress(toAddress);
		this.subject = subject;
		this.content = content;
	}

	public Mail(String fromAddress, String toAddress, StringBuilder ccAddress,
			StringBuilder bccAddress, String subject, String content,
			List<AttachBean> attachList) {
		this.fromAddress = fromAddress;
		addToAddress(toAddress);
		this.ccAddress = ccAddress;
		this.bccAddress = bccAddress;
		this.subject = subject;
		this.content = content;
		this.attachList = attachList;
	}

	/**
	 * 增加发送地址。多个地址以逗号分隔
	 * @param address 发送第一
	 */
	public void addToAddress(String address) {
		if (toAddress == null) {
			toAddress = new StringBuilder();
		}
		toAddress.append(address).append(",");
	}

	/**
	 * 增加抄送地址
	 * @param address 抄送地址
	 */
	public void addCCAddress(String address) {
		if (ccAddress == null) {
			ccAddress = new StringBuilder();
		}
		ccAddress.append(address).append(",");
	}

	/**
	 * 增加暗送地址
	 * @param address 新增的暗送地址
	 */
	public void addBccAddress(String address) {
		if (bccAddress == null) {
			bccAddress = new StringBuilder();
		}
		bccAddress.append(address).append(",");
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddress() {
		return toAddress.toString();
	}

	public void setToAddress(StringBuilder toAddress) {
		this.toAddress = toAddress;
	}

	public String getCcAddress() {
		return ccAddress.toString();
	}

	public void setCcAddress(StringBuilder ccAddress) {
		this.ccAddress = ccAddress;
	}

	public String getBccAddress() {
		return bccAddress.toString();
	}

	public void setBccAddress(StringBuilder bccAddress) {
		this.bccAddress = bccAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 获取附件列表
	 * @return 包含所有附件的列表
	 */
	public List<AttachBean> getAttachList() {
		return attachList;
	}

	public void setAttachList(List<AttachBean> attachList) {
		this.attachList = attachList;
	}

	/**
	 * 增加附件
	 * @param attachBean 新增的附件
	 */
	public void addAttachBean(AttachBean attachBean) {
		if (attachList == null) {
			attachList = new ArrayList<AttachBean>();
		}
		attachList.add(attachBean);
	}

	/**
	 * 移除附件
	 * @param attachBean 要移除的附件
	 */
	public void removeAttachBean(AttachBean attachBean) {
		if (attachList != null && attachList.size() > 0) {
			attachList.remove(attachBean);
		}
	}
}

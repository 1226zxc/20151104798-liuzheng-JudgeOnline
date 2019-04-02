package com.lz.web.dto;

import java.math.BigInteger;

import com.lz.web.po.User;

public class ProblemAnswerDTO {
	/**
	 * 所用代码语言
	 */
	private String codeLanguage;

	/**
	 * 提交问题的ID
	 */
	private BigInteger submitProblemId;

	/**
	 * 提交的用户
	 */
	private User user;

	/**
	 * 提交的代码
	 */
	private String code;

	public String getCodeLanguage() {
		return codeLanguage;
	}

	public void setCodeLanguage(String codeLanguage) {
		this.codeLanguage = codeLanguage;
	}

	public BigInteger getSubmitProblemId() {
		return submitProblemId;
	}

	public void setSubmitProblemId(BigInteger submitProblemId) {
		this.submitProblemId = submitProblemId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}

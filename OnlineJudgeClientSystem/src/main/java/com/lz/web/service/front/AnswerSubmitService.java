package com.lz.web.service.front;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lz.system.service.JavaSandboxService;
import com.lz.system.service.dto.JudgeProblemDTO;
import com.lz.system.service.dto.ProblemJudgeResult;
import com.lz.system.service.dto.ProblemJudgeResultItem;
import com.lz.web.constant.WebConstant;
import com.lz.web.dao.ProblemDao;
import com.lz.web.dao.SubmitRecordDao;
import com.lz.web.dao.UserDao;
import com.lz.web.dto.ProblemAnswerDTO;
import com.lz.web.exception.ServiceLogicException;
import com.lz.web.po.Problem;
import com.lz.web.po.SubmitRecord;
import com.lz.web.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import com.lz.constant.ConstantParameter;
import com.lz.util.DateUtil;
import com.lz.util.EncryptUtility;
import com.lz.util.JsonUtil;
import com.lz.util.Log4JUtil;
import com.lz.util.UUIDUtil;

@Service
public class AnswerSubmitService {
	@Autowired
	private SubmitRecordDao submitRecordDao;
	@Autowired
	private ProblemDao problemDao;
	@Autowired
	private UserDao userDao;

	/**
	 * 测评机服务对象，单例。
	 */
	private JavaSandboxService javaSandboxService;

	/**
	 * 包名的命名规范正则匹配表达式
	 */
	private Pattern packagePattern = Pattern.compile("^[ ]*package.*;");

	/**
	 * 类名命名规范正则匹配表达式：规定类名必须为Main
	 */
	private Pattern classNamePattern = Pattern.compile("public[ ]*class[ ]*Main[ ]*\\{");

	/**
	 * 方法一定是public static void main（）
	 */
	private Pattern mainMethodPattern = Pattern.compile("public[ ]*static[ ]*void[ ]*main");

	public AnswerSubmitService() {
		javaSandboxService = JavaSandboxService.getInstance();
	}

	/**
	 * 提交代码操作步骤
	 * - 保存源代码到服务器文件系统
	 * - 保存提交记录到数据库中
	 * - 触发判题请求
	 * @param dto
	 */
	public void submitAnswer(ProblemAnswerDTO dto) {
		// 验证代码的规范性
		checkCodeStandard(dto.getCode());

		User user = dto.getUser();
		// 组拼java文件名，并修改里面的主类名
		// u用户ID:时间毫秒值
		String javaFileName = "u" + dto.getUser().getUserId() + "_" + System.currentTimeMillis() + "Main";
		// 替换主类名为文件名
		String code = dto.getCode().replace("Main", javaFileName);

		// 创建当天的代码提交文件夹
		String today = DateUtil.getYYYYMMddToday();
		File dir = new File(user.getSourceFileRootPath() + File.separator + today);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Log4JUtil.logError(new RuntimeException("创建文件夹失败，无法保存用户代码"));
			}
		}

		FileOutputStream outputStream = null;
		String javaFilePath = dir.getAbsolutePath() + File.separator + javaFileName +
				WebConstant.DEFAULT_CODE_FILE_SUFFIX;
		try {
			outputStream = new FileOutputStream(javaFilePath);
			outputStream.write(code.getBytes());

			SubmitRecord record = new SubmitRecord();
			record.setIsAccepted(false);
			record.setCodeLanguage(dto.getCodeLanguage());
			record.setCodeFilePath(javaFilePath);
			record.setDetails("编译运行中");
			record.setScore(new Double(0));
			record.setSubmitProblemId(dto.getSubmitProblemId());
			record.setSubmitTime(new Date());
			record.setSubmitUserId(user.getUserId());
			record.setSubmitRecordTableName(user.getSubmitRecordTableName());
			submitRecordDao.add(record);

			// 发布判题请求
			sendAnswerToJudge(dto, javaFilePath, record);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("创建文件失败，无法保存用户代码");
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {

			}
		}
	}

	/**
	 * 验证提交的代码的正确性
	 * @param code 提交代码的字符串表达形式
	 * @return true表示代码没有问题，false表示代码有问题
	 */
	private void checkCodeStandard(String code) {
		Matcher matcher = packagePattern.matcher(code);
		if (matcher.find()) {
			throw new ServiceLogicException("不能拥有package语句");
		}

		matcher = classNamePattern.matcher(code);
		if (!matcher.find()) {
			throw new ServiceLogicException("主类名必须是Main");
		}

		matcher = mainMethodPattern.matcher(code);
		if (!matcher.find()) {
			throw new ServiceLogicException("没有静态的main方法入口");
		}
	}

	/**
	 * 发布判题请求。
	 * 对于测评机，只关心本次
	 * - 时间限制
	 * - 内存限制
	 * - 源代码路径
	 * - 测试用例输入输出文件路径
	 * @param dto 提交的答案信息
	 * @param javaFilePath 提交的源代码路径
	 * @param record
	 */
	private void sendAnswerToJudge(ProblemAnswerDTO dto, String javaFilePath, final SubmitRecord record) {
		// 先查询出，题目相关要求
		Problem problem = problemDao.findById(dto.getSubmitProblemId());
		JudgeProblemDTO judgeProblemDTO = new JudgeProblemDTO();
		judgeProblemDTO.setJavaFilePath(javaFilePath);
		judgeProblemDTO.setMemoryLimit(problem.getMemoryLimit());
		judgeProblemDTO.setTimeLimit(problem.getTimeLimit());
		judgeProblemDTO.setRunId(UUIDUtil.getUUID());

		// 获得测试输入输出文件，以便重定向标准输入和输出流
		List<String> inputPaths = getFileList(problem.getInputFileRootPath());
		List<String> outputPaths = getFileList(problem.getOutputFileRootPath());
		judgeProblemDTO.setProblemInputPathList(inputPaths);
		judgeProblemDTO.setProblemOutputPathList(outputPaths);
		judgeProblemDTO.setEvaluationResultHandler(
				new EvaluationResultHandler(record.getSubmitRecordTableName(),
						record.getSubmitId(), dto.getUser().getUserId(), problem));

		// 开始提交判题任务
		javaSandboxService.commitJudgementRequest(judgeProblemDTO, new JavaSandboxService.ErrorListener() {
			@Override
			public void onError(Exception exception) {
				record.setDetails(exception.getMessage());
				submitRecordDao.update(record);
			}
		});
	}

	/**
	 * 根据指定的目录路径，获取该路径下的所有文件路径，封装成List并返回。
	 * @param rootPath 目录路径
	 * @return 返回指定目录下的所有文件路径List
	 */
	private List<String> getFileList(String rootPath) {
		File inputFileDir = new File(rootPath);
		String[] fileNames = inputFileDir.list();
		List<String> inputPaths = new ArrayList<String>(fileNames.length);

		for (int i = 0; i < fileNames.length; i++) {
			inputPaths.add(rootPath + File.separator + fileNames[i]);
		}
		return inputPaths;
	}

	private class EvaluationResultHandler implements JavaSandboxService.EvaluationResultHandler {
		private String submitRecordTableName;
		private BigInteger submitRecordId;
		private Integer userId;
		private Problem problem;

		public EvaluationResultHandler(String submitRecordTableName, BigInteger submitRecordId,
									   Integer userId, Problem problem) {
			super();
			this.submitRecordTableName = submitRecordTableName;
			this.submitRecordId = submitRecordId;
			this.userId = userId;
			this.problem = problem;
		}

		@Override
		public void handleResult(ProblemJudgeResult problemJudgeResult) {
			SubmitRecord record = new SubmitRecord();
			record.setSubmitRecordTableName(submitRecordTableName);
			record.setSubmitId(submitRecordId);

			User user = new User();
			user.setUserId(userId);
			user.setLastSubmitTime(new Date());
			userDao.update(user);

			if (problemJudgeResult.getCorrectRate() >= 1.0) {
				record.setIsAccepted(true);
				// 更新这道题目的答对者总数，提交者总数，以及答对者ID编号集合
				problemDao.userSloveProblem(userId + WebConstant.PROBLEM_RIGHT_USER_ID_GAP,
						problem.getProblemId());
			} else {
				record.setIsAccepted(false);
				// 提交者总数
				problemDao.increaseSubmitProblemCount(problem.getProblemId());
			}

			record.setScore(new Double(problemJudgeResult.getCorrectRate() * 100));

			List<ProblemJudgeResultItem> problemJudgeResultItems = problemJudgeResult.getProblemJudgeResultItems();
			ProblemJudgeResultItem tempResultItem = null;
			// 先加密里面的输入输出文件路径
			for (int i = 0; i < problemJudgeResultItems.size(); i++) {
				tempResultItem = problemJudgeResultItems.get(i);
				try {
					tempResultItem.setInputFilePath(Base64Utils.encodeToUrlSafeString(EncryptUtility
							.AESEncoding(ConstantParameter.PROBLEM_STANDARD_FILE_PATH_SEED,
									tempResultItem.getInputFilePath()).getBytes()));

					tempResultItem.setOutputFilePath(Base64Utils.encodeToUrlSafeString(EncryptUtility
											.AESEncoding(ConstantParameter.PROBLEM_STANDARD_FILE_PATH_SEED,
													tempResultItem.getOutputFilePath()).getBytes()));
				} catch (Exception e) {
					Log4JUtil.logError(e);
				}
			}
			String details = JsonUtil.toJson(problemJudgeResultItems);
			record.setDetails(details);

			submitRecordDao.update(record);
		}
	}
}



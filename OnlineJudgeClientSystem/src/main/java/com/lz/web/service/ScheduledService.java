package com.lz.web.service;

import com.lz.constant.ConstantParameter;
import com.lz.util.Log4JUtil;
import com.lz.web.service.admin.AdminUserService;
import com.lz.web.service.front.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * 定时调度方法
 * @author 刘铮
 */
@Service
public class ScheduledService {
	@Autowired
	private UserService userService;
	@Autowired
	private AdminUserService adminUserService;

	/**
	 * 每天凌晨6点执行
	 */
	@Scheduled(cron = "0 0 6 * * ? ")
	public void updateUserLeaderboardCache() {
		userService.updateUserLeaderboardCache();
	}

	/**
	 * 每天凌晨一点执行
	 */
	@Scheduled(cron = "0 0 1 * * ? ")
	public void countUserData() {
		adminUserService.countUserData();
	}

	/**
	 * 每兩个小时定时清理一下，用于放置class文件的文件夹内过期的class文件
	 */
	@Scheduled(cron = "0 0 0/2 * * ?")
	public void clearClassFiles() {
		File dir = new File(ConstantParameter.CLASS_FILE_ROOT_PATH);
		if (!dir.exists()) {
			try {
				dir.createNewFile();
			} catch (IOException e) {
				Log4JUtil.logError(e);
			}
			return;
		}

		File[] listFiles = dir.listFiles();
		if (listFiles == null || listFiles.length < 1) {
			return;
		}
		for (File file : listFiles) {
			try {
				file.delete();
			} catch (Exception e) {
				Log4JUtil.logError(e);
			}
		}
	}
}

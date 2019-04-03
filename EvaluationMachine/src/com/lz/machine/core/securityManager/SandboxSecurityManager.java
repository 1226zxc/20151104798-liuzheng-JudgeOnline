package com.lz.machine.core.securityManager;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

import com.lz.machine.constant.ConstantParameter;

public class SandboxSecurityManager extends SecurityManager {
	/**
	 * 防止有人非法退出虚拟机
	 * 检查JVM是否合法退出，传进来的参数如果是ConstantParameter.EXIT_VALUE
	 * 则退出时合法的，否则是非法退出将会抛出异常
	 * @param status 合法退出状态码
	 */
	@Override
	public void checkExit(int status) {
		if (status != ConstantParameter.EXIT_VALUE) {
			throw new RuntimeException("非法退出，不允许退出虚拟机");
		}
		super.checkExit(status);
	}

	/**
	 * 当应用程序对指定好的操作执行之前，会触发这个函数来检查
	 * 是否有这个权限，传进来的perm就代表想要进行的权限操作
	 * @param perm 请求的权限
     */
	@Override
	public void checkPermission(Permission perm) {
		conformPermissionToSandbox(perm);
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
		conformPermissionToSandbox(perm);
	}

	/**
	 * 只给与必要的权限（比如读取，获取某些信息等），避免提交者进行非法操作。
	 * 只允许读取文件获取文件信息，不能修改文件内容
	 * 
	 * @param perm 请求的，需要检查的权限
	 */
	private void conformPermissionToSandbox(Permission perm) {
		if (perm instanceof SecurityPermission) {
			if (perm.getName().startsWith("getProperty")) {
				return;
			}
		} else if (perm instanceof PropertyPermission) {
			if (perm.getActions().equals("read")) {
				return;
			}
		} else if (perm instanceof FilePermission) {
			if (perm.getActions().equals("read")) {
				return;
			}
		} else if (perm instanceof RuntimePermission
				|| perm instanceof ReflectPermission
				|| perm instanceof LoggingPermission) {
			return;
		}
		
		throw new SecurityException(perm.toString() + "无法使用该权限");
	}
}

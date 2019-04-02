package com.lz.web.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lz.web.permission.Permissions;

/**
 * 用于修饰Controller方法权限的注解
 * 
 * @author 刘铮
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AdminPermission {
	Permissions[] value();
}

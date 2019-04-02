package com.lz.util;

import org.apache.log4j.Logger;

import com.lz.constant.Log4JConstant;

/**
 * Log4J工具类
 *
 * @author 刘铮
 */
public class Log4JUtil {
    public static void logError(Throwable t) {
        Logger.getLogger(Log4JConstant.errorLogName).error("", t);
    }
}
package com.mogujie.trade.utils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONWriter;

/**
 * 中间件日志
 * 
 * @author SHOUSHEN LUAN create date: 2017年1月7日
 */
public class TShardingLog {
	private final static Logger LOGGER = LoggerFactory.getLogger(TShardingLog.class);
	public static final String SYSTEM_INFO = "[TSharding]";

	public static void warn(String msg, Object... param) {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn(SYSTEM_INFO + msg, param);
		}
	}

	public static void info(String msg, Object... param) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(SYSTEM_INFO + msg, param);
		}
	}

	public static void error(String msg, Object... param) {
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error(SYSTEM_INFO + msg, param);
		}
	}

	public static void error(String msg, Throwable th) {
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error(SYSTEM_INFO + msg, th);
		}
	}

	public static void debug(String msg, Object... param) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SYSTEM_INFO + msg, param);
		}
	}

	/**
	 * 记录慢DB操作
	 * 
	 * @param mapper
	 * @param method
	 * @param args
	 * @param isSharding
	 * @param dateSource
	 * @param useTime
	 */
	public static void slowCall(Class<?> mapper, Method method, Object[] args, boolean isSharding, String dateSource,
			long useTime) {
		LOGGER.warn("prefix:slowDB|mapper:{}|method:{}|args:{}|isSharding:{}|dateSource:{}|useTime:{}",
				mapper.getName(), method.getName(), toString(args), isSharding, dateSource, useTime);
	}

	private static String toString(Object... objs) {
		JSONWriter out = new JSONWriter();
		out.writeArrayStart();
		for (int i = 0; i < objs.length; i++) {
			if (i != 0) {
				out.writeComma();
			}
			Object value = objs[i];
			if (value == null) {
				out.writeNull();
			} else {
				if (value instanceof String) {
					String text = (String) value;
					if (text.length() > 100) {
						out.writeString(text.substring(0, 97) + "...");
					} else {
						out.writeString(text);
					}
				} else if (value instanceof Number) {
					out.writeObject(value);
				} else if (value instanceof java.util.Date) {
					out.writeObject(value);
				} else if (value instanceof Boolean) {
					out.writeObject(value);
				} else if (value instanceof InputStream) {
					out.writeString("<InputStream>");
				} else if (value instanceof NClob) {
					out.writeString("<NClob>");
				} else if (value instanceof Clob) {
					out.writeString("<Clob>");
				} else if (value instanceof Blob) {
					out.writeString("<Blob>");
				} else {
					out.writeString('<' + value.getClass().getName() + '>');
				}
			}
		}
		out.writeArrayEnd();
		return out.toString();
	}
}

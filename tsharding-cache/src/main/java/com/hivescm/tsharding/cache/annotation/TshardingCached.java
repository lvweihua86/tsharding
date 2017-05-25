package com.hivescm.tsharding.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tsharding 缓存注解
 * 
 * @author SHOUSHEN LUAN
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TshardingCached {
	/**
	 * 生成CACHE key的参数，每个参数之间使用“,”分割
	 * <p>
	 * 例如：“0,1.id,2.name”
	 * </p>
	 */
	String params() default "";

	/**
	 * 过期时间（单位：秒）
	 */
	int expire() default 60;

	/**
	 * 是否Cache NULL value
	 */
	boolean cacheNull() default false;

	/**
	 * 当cacheNull()等于true时，且注解方法调用返回值为null时，则使用“<NULL>”作为Null值存入redis
	 */
	String EMPTY = "<NULL>";
}

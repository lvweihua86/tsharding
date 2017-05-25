package com.hivescm.tsharding.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 清楚Tsharding缓存注解
 * 
 * @author SHOUSHEN LUAN
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TshardingCacheEvicted {
	/**
	 * 生成CACHE key的参数，每个参数之间使用“,”分割
	 * <p>
	 * 例如：“0,1.id,2.name”
	 * </p>
	 */
	String params() default "";
}

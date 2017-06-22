package com.hivescm.tsharding.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 清楚Tsharding缓存注解(只对Mapper接口方法上使用生效)
 * 
 * @author SHOUSHEN LUAN
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MapperCacheEvicted {
	/**
	 * 生成cache key的一部分
	 * 
	 * @return
	 */
	String[] key() default {};

	/**
	 * 生成CACHE key的参数，每个参数之间使用“,”分割
	 * <p>
	 * 例如：“0,1.id,2.name”
	 * </p>
	 */
	String[] params() default {};
}

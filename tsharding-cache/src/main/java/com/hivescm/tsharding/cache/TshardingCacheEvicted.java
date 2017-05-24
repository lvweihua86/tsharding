package com.hivescm.tsharding.cache;

public @interface TshardingCacheEvicted {
	/**
	 * 生成CACHE key的参数，每个参数之间使用“,”分割
	 * <p>
	 * 例如：“0,1.id,2.name”
	 * </p>
	 */
	String params() default "";
}

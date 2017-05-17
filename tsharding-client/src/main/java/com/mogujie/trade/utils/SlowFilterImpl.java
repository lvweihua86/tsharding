package com.mogujie.trade.utils;

import java.lang.reflect.Method;

import com.mogujie.trade.tsharding.route.orm.base.Invocation;

public class SlowFilterImpl implements TshardingFilter {
	/**
	 * slow DB标准,单位毫秒，默认值
	 */
	private static long times = 50;

	/**
	 * 调整slow DB标准,单位毫秒
	 * 
	 * @param times
	 */
	public static void setSlowTimes(long times) {
		if (times > 0)
			SlowFilterImpl.times = times;
	}

	@Override
	public void filter(Invocation invocation, long startTime, long endTime) {
		long useTime = endTime - startTime;
		if (useTime >= times) {
			String dateSource = invocation.getDataSourceRouting().dataSource();
			boolean isSharding = invocation.isSharding();
			Object[] args = invocation.getArgs();
			Class<?> mapper = invocation.getMapperClass();
			Method method = invocation.getMethod();
			TShardingLog.slowCall(mapper, method, args, isSharding, dateSource, useTime);
		}

	}

}

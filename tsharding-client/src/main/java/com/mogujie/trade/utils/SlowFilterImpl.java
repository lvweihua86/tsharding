package com.mogujie.trade.utils;

import java.lang.reflect.Method;

import com.mogujie.trade.tsharding.route.orm.base.Invocation;

public class SlowFilterImpl implements TshardingFilter {
	@Override
	public void filter(Invocation invocation, long startTime, long endTime) {
		long useTime = endTime - startTime;
		if (useTime > 2) {
			String dateSource = invocation.getDataSourceRouting().dataSource();
			boolean isSharding = invocation.isSharding();
			Object[] args = invocation.getArgs();
			Class<?> mapper = invocation.getMapperClass();
			Method method = invocation.getMethod();
			TShardingLog.slowCall(mapper, method, args, isSharding, dateSource, useTime);
		}

	}

}

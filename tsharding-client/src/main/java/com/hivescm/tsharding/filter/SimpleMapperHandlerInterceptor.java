package com.hivescm.tsharding.filter;

import com.mogujie.trade.utils.TShardingLog;

public class SimpleMapperHandlerInterceptor implements MapperHandlerInterceptor {

	@Override
	public Object invoker(InvocationProxy invocation) throws Throwable {
		long start = System.currentTimeMillis();
		try {
			return invocation.doInvoker();
		} finally {
			long useTime = System.currentTimeMillis() - start;
			TshardingHander hander = new TshardingHander(invocation.getInvoker(), useTime);
			TShardingLog.getLogger().info(hander.toString());
		}
	}

}

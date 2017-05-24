package com.mogujie.route.rule;

import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.utils.EnhanceMapperMethodUtils;

public abstract class AbstractRouteRule<T> implements RouteRule<T> {
	@Override
	public final int getSegemation(DataSourceRouting routing, T routeParam) {
		int tableSuffix = this.getTableSuffix(routing, routeParam);
		return EnhanceMapperMethodUtils.getSegemation(routing.tables(), tableSuffix);
	}
	
	public String fillDataSourceBit(long shardingDataSourceSuffix) {
		// 默认数据源sharding后缀，如果需要个性化处理的话，自行实现
		return String.valueOf(shardingDataSourceSuffix);
	}
}

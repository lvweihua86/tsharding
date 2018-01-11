package com.mogujie.service.tsharding.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mogujie.route.rule.CRC32RouteRule;
import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.tsharding.annotation.ShardingExtensionMethod;
import com.mogujie.trade.tsharding.annotation.parameter.ShardingParam;

@DataSourceRouting(dataSource = "user", isReadWriteSplitting = false, table = "user_order", routeRule = CRC32RouteRule.class, tables = 3, databases = 2)
public interface UserOrderMapper {
	@ShardingExtensionMethod
	public List<Map<String, Object>> join_test(@Param("id") @ShardingParam long id);
}

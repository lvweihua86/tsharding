package com.hivescm.tsharding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.hivescm.tsharding.scan.ScanMapperUtils;
import com.mogujie.trade.tsharding.route.orm.MapperShardingInitializer;

@Configuration()
@Order(10)
public class SpringBootMapperShardingInitializer extends MapperShardingInitializer {
	public SpringBootMapperShardingInitializer() {
		super();
		setNeedEnhancedClasses(ScanMapperUtils.scanMapper().buildEnhancedMapper());
	}
}

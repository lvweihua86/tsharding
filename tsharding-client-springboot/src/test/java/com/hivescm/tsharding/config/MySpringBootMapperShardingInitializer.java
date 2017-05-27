package com.hivescm.tsharding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.mogujie.service.tsharding.mapper.ShopOrderMapper;
import com.mogujie.service.tsharding.mapper.UserMapper;
@Order(10)
@Configuration()
public class MySpringBootMapperShardingInitializer extends SpringBootMapperShardingInitializer {
	@Override
	public Class<?>[] loadEnhancedMapper() {
		return new Class<?>[] { ShopOrderMapper.class, UserMapper.class };
	}

}

package com.hivescm.tsharding.config;

import org.springframework.context.annotation.Configuration;

import com.mogujie.service.tsharding.mapper.ShopOrderMapper;
import com.mogujie.service.tsharding.mapper.UserMapper;
@Configuration()
public class MySpringBootMapperShardingInitializer extends SpringBootMapperShardingInitializer {
	/**
	 * 装载增强Mapper接口
	 */
	@Override
	public Class<?>[] loadEnhancedMapper() {
		return new Class<?>[] { ShopOrderMapper.class, UserMapper.class };
	}

}

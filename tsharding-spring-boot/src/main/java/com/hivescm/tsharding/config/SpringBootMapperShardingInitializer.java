package com.hivescm.tsharding.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import com.hivescm.tsharding.scan.ScanMapperUtils;
import com.mogujie.trade.tsharding.route.orm.MapperShardingInitializer;

@Configuration()
public class SpringBootMapperShardingInitializer extends MapperShardingInitializer {
	private boolean isNeedEnhancedMapper = false;

	public SpringBootMapperShardingInitializer() {
		super();
		String enhancedMapper = ScanMapperUtils.scanMapper().buildEnhancedMapper();
		if (StringUtils.isNotBlank(enhancedMapper)) {
			isNeedEnhancedMapper=true;
			setNeedEnhancedClasses(enhancedMapper);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (isNeedEnhancedMapper) {
			super.setApplicationContext(applicationContext);
		}
	}
}

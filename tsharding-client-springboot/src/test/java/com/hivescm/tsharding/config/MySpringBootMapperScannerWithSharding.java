package com.hivescm.tsharding.config;

import org.springframework.context.annotation.Configuration;

@Configuration()
public class MySpringBootMapperScannerWithSharding extends SpringBootMapperScannerWithSharding {
	/**
	 * 装载Mapper xml资源配置
	 */
	@Override
	public String loadMapperXmlResource() {
		return "classpath*:sqlmap/*/*-mapper.xml";
	}

}

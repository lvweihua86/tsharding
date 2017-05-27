package com.hivescm.tsharding.config;

import org.springframework.context.annotation.Configuration;

@Configuration()
public class MySpringBootMapperScannerWithSharding extends SpringBootMapperScannerWithSharding {

	@Override
	public String loadMapperXmlResource() {
		return "classpath*:sqlmap/*/*-mapper.xml";
	}

}

package com.hivescm.tsharding.config;

import org.springframework.stereotype.Component;

import com.mogujie.trade.db.HikariDataSourceFactory;

@Component
public class SpringBootHikariDataSourceFactory extends HikariDataSourceFactory {
	public SpringBootHikariDataSourceFactory() {
		super();
	}

}

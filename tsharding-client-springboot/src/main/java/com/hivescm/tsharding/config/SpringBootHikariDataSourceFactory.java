package com.hivescm.tsharding.config;

import org.springframework.context.annotation.Configuration;

import com.mogujie.trade.db.HikariDataSourceFactory;

@Configuration()
public class SpringBootHikariDataSourceFactory extends HikariDataSourceFactory {
	public SpringBootHikariDataSourceFactory() {
		super();
	}

}

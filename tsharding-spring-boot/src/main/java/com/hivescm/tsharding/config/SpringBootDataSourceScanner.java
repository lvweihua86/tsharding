//package com.hivescm.tsharding.config;
//
//import javax.sql.DataSource;
//
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.DependsOn;
//import org.springframework.stereotype.Component;
//
//import com.mogujie.trade.db.DataSourceFactory;
//import com.mogujie.trade.db.DataSourceScanner;
//
//@Component
//@DependsOn(value = "hikariDataSourceFactory")
//public class SpringBootDataSourceScanner extends DataSourceScanner {
//
//	public SpringBootDataSourceScanner() {
//		super();
//	}
//
//	@Override
//	public void setApplicationContext(ApplicationContext context) throws BeansException {
//		DataSourceFactory<?> dataSourceFactory = context.getAutowireCapableBeanFactory()
//				.getBean(DataSourceFactory.class);
//		setDataSourceFactory(dataSourceFactory);
//		super.setApplicationContext(context);
//	}
//
//	@Override
//	public void setDataSourceFactory(DataSourceFactory<? extends DataSource> dataSourceFactory) {
//		super.setDataSourceFactory(dataSourceFactory);
//	}
//}

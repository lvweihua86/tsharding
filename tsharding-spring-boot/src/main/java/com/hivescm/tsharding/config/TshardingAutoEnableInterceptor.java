//package com.hivescm.tsharding.config;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.annotation.Configuration;
//
//import com.hivescm.tsharding.cache.CacheMapperHandlerInterceptor;
//import com.mogujie.tsharding.filter.HandlerInterceptorAdapterFactory;
//
///***
// * 使用Spring Boot注册Tsharding拦截器
// * 
// * @author SHOUSHEN LUAN
// *
// */
//@Configuration
//public class TshardingAutoEnableInterceptor implements ApplicationContextAware {
//	private static final Logger LOGGER = LoggerFactory.getLogger(TshardingAutoEnableInterceptor.class);
//
//	@Override
//	public void setApplicationContext(ApplicationContext context) throws BeansException {
//		LOGGER.warn("Enable TshardingAutoEnableInterceptor start......");
//		AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
//		CacheMapperHandlerInterceptor interceptor = beanFactory.createBean(CacheMapperHandlerInterceptor.class);
//		HandlerInterceptorAdapterFactory.registerInterceptor(interceptor);
//		LOGGER.warn("Enable TshardingAutoEnableInterceptor done......");
//	}
//}

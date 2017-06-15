package com.hivescm.tsharding.config;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import com.mogujie.distributed.transction.ChainedTransactionInteceptor;
import com.mogujie.distributed.transction.DynamicTransctionManagerFactory;
import com.mogujie.trade.db.DataSourceScanner;
import com.mogujie.trade.db.DruidDataSourceFactory;
import com.mogujie.trade.db.HikariDataSourceFactory;
import com.mogujie.trade.db.ReadWriteSplittingAdvice;
import com.mogujie.trade.tsharding.route.orm.MapperScannerWithSharding;
import com.mogujie.trade.tsharding.route.orm.MapperShardingInitializer;
import com.mogujie.tsharding.filter.HandlerInterceptorAdapterFactory;
import com.mogujie.tsharding.filter.MapperHandlerInterceptor;

class EnableConfigRegistrar
		implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware {

	public EnableConfigRegistrar() {
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		Map<String, Object> defaultAttrs = metadata.getAnnotationAttributes(EnableTSharding.class.getName(), true);
		registerMapper(defaultAttrs, registry);
		registerEnhancedMappers(defaultAttrs, registry);
		registerDataSourceFactory(registry, defaultAttrs);
		registerDataSourceScanner(registry);
		registerReadWriteSplittingAdvice(registry);
		registerMapperHandlerInterceptor(registry, defaultAttrs);
		registerDynamicTransctionManagerFactory(registry);
		registerChainedTransactionInteceptor(registry);
	}

	/**
	 * 注册链式事物管理器
	 * 
	 * @param registry
	 */
	private void registerChainedTransactionInteceptor(BeanDefinitionRegistry registry) {
		BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(ChainedTransactionInteceptor.class);
		String name = ChainedTransactionInteceptor.class.getSimpleName();
		registry.registerBeanDefinition(name, definitionBuilder.getBeanDefinition());
	}

	/**
	 * 注册动态事物管理器工厂
	 * 
	 * @param registry
	 */
	private void registerDynamicTransctionManagerFactory(BeanDefinitionRegistry registry) {
		BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(DynamicTransctionManagerFactory.class);
		String name = DynamicTransctionManagerFactory.class.getSimpleName();
		registry.registerBeanDefinition(name, definitionBuilder.getBeanDefinition());
	}

	/**
	 * 注册Mapper拦截器
	 * 
	 * @param registry
	 */
	@SuppressWarnings("unchecked")
	private void registerMapperHandlerInterceptor(BeanDefinitionRegistry registry, Map<String, Object> defaultAttrs) {
		String interceptorName = (String) defaultAttrs.get("interceptor");
		if (interceptorName == null) {
			return;// 禁用拦截器
		}
		Class<? extends MapperHandlerInterceptor> interceptor = null;
		try {
			interceptor = (Class<? extends MapperHandlerInterceptor>) Class.forName(interceptorName);

			BeanDefinitionBuilder interceptorBuilder = BeanDefinitionBuilder.genericBeanDefinition(interceptor);
			String name = interceptor.getClass().getSimpleName();
			registry.registerBeanDefinition(name, interceptorBuilder.getBeanDefinition());

			BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(HandlerInterceptorAdapterFactory.class);
			adapterBuilder.addConstructorArgReference(name);
			String handleName = HandlerInterceptorAdapterFactory.class.getSimpleName();
			registry.registerBeanDefinition(handleName, adapterBuilder.getBeanDefinition());

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 注册Mapper
	 * 
	 * @param defaultAttrs
	 * @param registry
	 */
	private void registerMapper(Map<String, Object> defaultAttrs, BeanDefinitionRegistry registry) {
		String[] mapperPackage = (String[]) defaultAttrs.get("mapperPackage");
		BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(MapperScannerWithSharding.class);
		definitionBuilder.addPropertyValue("packageName", toString(mapperPackage));
		definitionBuilder.addPropertyValue("mapperLocations", defaultAttrs.get("mapperLocations"));
		String myBatisConfig = (String) defaultAttrs.get("configLocation");
		if (StringUtils.isNoneBlank(myBatisConfig)) {
			definitionBuilder.addPropertyValue("configLocation", defaultAttrs.get("configLocation"));
		}
		registry.registerBeanDefinition("MapperScannerWithSharding", definitionBuilder.getBeanDefinition());
	}

	private static String toString(String[] strs) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < strs.length; i++) {
			builder.append(strs[i]);
			if (strs.length > i + 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	/**
	 * 注册增强Mapper
	 * 
	 * @param defaultAttrs
	 * @param registry
	 */
	private void registerEnhancedMappers(Map<String, Object> defaultAttrs, BeanDefinitionRegistry registry) {
		String[] enhancedMappers = (String[]) defaultAttrs.get("enhancedMappers");
		if (enhancedMappers != null && enhancedMappers.length > 0) {
			BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(MapperShardingInitializer.class);
			definitionBuilder.addPropertyValue("needEnhancedClasses", toString(enhancedMappers));
			registry.registerBeanDefinition("MapperShardingInitializer", definitionBuilder.getBeanDefinition());
		}
	}

	/**
	 * 注册DataSource工厂
	 * 
	 * @param registry
	 */
	private void registerDataSourceFactory(BeanDefinitionRegistry registry, Map<String, Object> defaultAttrs) {
		DataSourceType dataSourceType = (DataSourceType) defaultAttrs.get("dataSourceType");
		if (DataSourceType.Druid.equals(dataSourceType)) {// 阿里连接池
			BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(DruidDataSourceFactory.class);
			registry.registerBeanDefinition("dataSourceFactory", definitionBuilder.getBeanDefinition());
		} else {
			BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(HikariDataSourceFactory.class);
			registry.registerBeanDefinition("dataSourceFactory", definitionBuilder.getBeanDefinition());
		}
	}

	/**
	 * 注册DataSource
	 * 
	 * @param registry
	 */
	private void registerDataSourceScanner(BeanDefinitionRegistry registry) {
		BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceScanner.class);
		definitionBuilder.addPropertyReference("dataSourceFactory", "dataSourceFactory");
		registry.registerBeanDefinition("dataSourceScanner", definitionBuilder.getBeanDefinition());
	}

	private void registerReadWriteSplittingAdvice(BeanDefinitionRegistry registry) {
		BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(ReadWriteSplittingAdvice.class);
		registry.registerBeanDefinition("ReadWriteSplittingAdvice", definitionBuilder.getBeanDefinition());
	}

	@Override
	public void setEnvironment(Environment environment) {
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
	}
}

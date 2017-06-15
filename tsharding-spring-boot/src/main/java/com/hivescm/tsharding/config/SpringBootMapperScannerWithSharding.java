//package com.hivescm.tsharding.config;
//
//import java.io.IOException;
//
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//
//import com.mogujie.trade.tsharding.route.orm.MapperScannerWithSharding;
//
///**
// * 处理Mapper接口
// * 
// * @author SHOUSHEN LUAN
// *
// */
//@Configuration()
//public abstract class SpringBootMapperScannerWithSharding extends MapperScannerWithSharding {
//	/**
//	 * 装载Mybatis配置文件
//	 * 
//	 * @return
//	 */
//	public Resource loadMybatisConfig() {
//		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//		return resolver.getResource("classpath:mybatis-config.xml");
//	}
//
//	/**
//	 * 装载Mybatis Mapper配置
//	 * 
//	 * @return
//	 * @throws IOException
//	 */
//	private Resource[] loadResources() throws IOException {
//		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//		Resource[] resources = resolver.getResources(this.loadMapperXmlResource());
//		return resources;
//	}
//
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		super.setPackageName(getMapperPackages());
//		super.setConfigLocation(loadMybatisConfig());
//		try {
//			super.setMapperLocations(loadResources());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		super.afterPropertiesSet();
//
//	}
//
//	/**
//	 * 重写该方法设置Mybatis Mapper包路径，多个包路径使用“,“逗号分隔
//	 * 
//	 * @return
//	 */
//	public abstract String getMapperPackages();
//
//	/**
//	 * 装载Mybatis mapper.xml资源配置
//	 * <p>
//	 * <code>
//	 * public String loadMapperXmlResource() {
//		return "classpath*:sqlmap\/*\/*-mapper.xml";
//	 * }
//	 * </code>
//	 * </p>
//	 * 
//	 * @return
//	 */
//	public String loadMapperXmlResource() {
//		return "classpath*:sqlmap/*-mapper.xml";
//	}
//
//	@Override
//	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//		super.postProcessBeanFactory(beanFactory);
//	}
//}

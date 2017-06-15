//package com.hivescm.tsharding.config;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Configuration;
//
//import com.mogujie.trade.tsharding.route.orm.MapperShardingInitializer;
//
///**
// * 增强Mapper接口
// * 
// * @author SHOUSHEN LUAN
// *
// */
//@Configuration()
//public abstract class SpringBootMapperShardingInitializer extends MapperShardingInitializer {
//	private boolean isNeedEnhancedMapper = false;
//
//	public SpringBootMapperShardingInitializer() {
//		super();
//		String enhancedMapper = buildEnhancedMapper();
//		if (StringUtils.isNotBlank(enhancedMapper)) {
//			isNeedEnhancedMapper = true;
//			setNeedEnhancedClasses(enhancedMapper);
//		}
//	}
//
//	@Override
//	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//		if (isNeedEnhancedMapper) {
//			super.setApplicationContext(applicationContext);
//		}
//	}
//
//	/**
//	 * 获取增强Mapper类
//	 * 
//	 * @return
//	 */
//	public abstract Class<?>[] getEnhancedMappers();
//
//	/**
//	 * 生成增强类字符串形式 生成增强类Mapper
//	 * <p>
//	 * 例如：
//	 * com.mogujie.service.tsharding.mapper.ShopOrderMapper,com.mogujie.service.tsharding.mapper.UserMapper
//	 * </p>
//	 * 
//	 * @return
//	 */
//	private String buildEnhancedMapper() {
//		StringBuilder builder = new StringBuilder(1024);
//		Class<?>[] clazzs = getEnhancedMappers();
//		for (int i = 0; i < clazzs.length; i++) {
//			Class<?> clazz = clazzs[i];
//			builder.append(clazz.getName());
//			if (clazzs.length > i + 1) {
//				builder.append(",");
//			}
//		}
//		return builder.toString();
//	}
//}

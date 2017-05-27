package com.hivescm.tsharding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.mogujie.trade.tsharding.route.orm.MapperShardingInitializer;

@Configuration()
@Order(10)
public abstract class SpringBootMapperShardingInitializer extends MapperShardingInitializer {
	public SpringBootMapperShardingInitializer() {
		super();
		setNeedEnhancedClasses(buildEnhancedMapper());
	}

	/**
	 * 生成增强类Mapper
	 * <p>
	 * 例如：
	 * com.mogujie.service.tsharding.mapper.ShopOrderMapper,com.mogujie.service.tsharding.mapper.UserMapper
	 * </p>
	 * 
	 * @return
	 */
	private String buildEnhancedMapper() {
		Class<?>[] classes = loadEnhancedMapper();
		StringBuilder builder = new StringBuilder(1024);
		for (int i = 0; i < classes.length; i++) {
			builder.append(classes[i].getName());
			if (classes.length > i + 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	/**
	 * 装载增强Mapper
	 * <p>
	 * 示例： <code>
	 * 	public Class<?>[] loadEnhancedMapper() {
	 *		return new Class<?>[] { ShopOrderMapper.class, UserMapper.class };
	 *	}
	 * </code>
	 * </p>
	 * 
	 * @return
	 */
	public abstract Class<?>[] loadEnhancedMapper();
}

package com.mogujie.trade.tsharding.route.orm.base;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.fw.route.rule.RouteRule;
import com.fw.route.rule.RouteRuleFactory;
import com.fw.route.rule.ShardingUtils;
import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.hander.MapperFactory;
import com.mogujie.trade.hander.MapperFactory.ShardingHanderEntry;
import com.mogujie.trade.hander.ShardingHander;
import com.mogujie.trade.utils.EnhanceMapperMethodUtils;

class ShardingInvocation implements Invocation {
	/**
	 * Mapper class
	 */
	private final Class<?> rawMapper;
	/**
	 * sharding mapper class
	 */
	private final Class<?> shardingMapper;
	/**
	 * sharding mapper method
	 */
	private final Method routeMethod;
	private final Method rawMethod;
	private final Object[] args;
	private final ShardingMeta shardingMeta;
	private final MapperBasicConfig config;
	private final ShardingHanderEntry shardingHander;
	/***
	 * 路由规则
	 */
	private final RouteRule<Object> rule;
	/**
	 * 原始路由参数
	 */
	private final Object rawShardingParam;
	/**
	 * 实际路由参数
	 */
	private final Object routeParam;

	public ShardingInvocation(Class<?> rawMapper, Method rawMethod, Object[] args) throws IllegalAccessException,
			IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		this.rawMapper = rawMapper;
		this.rawMethod = rawMethod;
		this.args = args;
		this.shardingHander = MapperFactory.getShardingHanderEntry(rawMapper, rawMethod);
		this.rawShardingParam = shardingHander.getRouteParam(args);
		this.rule = RouteRuleFactory.getRouteRule(shardingHander.getMappedClass());
		this.routeParam = parserShardingValue();
		this.shardingMeta = markShardingMeta();
		this.shardingMapper = markShardingMapper();
		this.config = markMapperBasicConfig();
		this.routeMethod = markRouteMethod();
	}

	private MapperBasicConfig markMapperBasicConfig() {
		return new MapperBasicConfig(getMapperClass(), getShardingMappingClass(), getShardingMeta().getSchemaName());
	}

	private Object parserShardingValue() throws IllegalArgumentException, IllegalAccessException {
		String fieldName = shardingHander.getShardingParam().value();
		return ShardingUtils.parserShardingValue(rawShardingParam, fieldName, rule);
	}

	private ShardingMeta markShardingMeta() throws IllegalArgumentException, IllegalAccessException {
		ShardingHander hander = getShardingHander();
		ShardingMeta shardingMeta = new ShardingMeta();
		shardingMeta.setShardingParam(hander.getShardingValue());
		shardingMeta.setTableSuffix(hander.getTableNameSuffix());
		shardingMeta.setSchemaName(hander.schemaName());
		return shardingMeta;
	}

	@Override
	public Method getMethod() {
		return this.rawMethod;
	}

	@Override
	public Object[] getArgs() {
		return this.args;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShardingInvocation [");
		if (rawMethod != null) {
			builder.append("method=").append(rawMethod).append(", ");
		}
		if (args != null) {
			builder.append("args=").append(Arrays.toString(args));
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Method getRouteMethod() {
		return this.routeMethod;
	}

	private Method markRouteMethod() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException,
			NoSuchMethodException, SecurityException {
		ShardingMeta shardingMeta = getShardingMeta();
		Class<?> shardingMapper = getShardingMappingClass();
		Method shareMethod = shardingMapper.getMethod(rawMethod.getName() + shardingMeta.getTableSuffix(),
				rawMethod.getParameterTypes());
		return shareMethod;
	}

	private ShardingHander getShardingHander() {
		DataSourceRouting routing = shardingHander.getRouting();
		Class<?> handerClass = routing.shardingHander();
		try {
			ShardingHander hander = (ShardingHander) handerClass.getConstructor(Class.class, Method.class, Object[].class)
					.newInstance(rawMapper, rawMethod, args);
			return hander;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ShardingMeta getShardingMeta() {
		return shardingMeta;
	}

	@Override
	public Class<?> getMapperClass() {
		return rawMapper;
	}

	/**
	 * 创建shardingMapper class
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?> markShardingMapper() throws ClassNotFoundException {
		String mapperClassName = rawMapper.getCanonicalName();
		// 计算分段
		int segemation = rule.getSegemation(shardingHander.getRouting(), routeParam);
		String shardingMapper = EnhanceMapperMethodUtils.markShardingClass(mapperClassName, rawMethod.getName(),
				segemation);
		// 增强后的接口
		return Class.forName(shardingMapper);
	}

	@Override
	public Class<?> getShardingMappingClass() {
		return shardingMapper;
	}

	@Override
	public DataSourceRouting getDataSourceRouting() {
		return this.shardingHander.getRouting();
	}

	@Override
	public MapperBasicConfig getMapperConfig() {
		return config;
	}

	public boolean isSharding() {
		return true;
	}

}

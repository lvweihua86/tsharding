package com.mogujie.trade.tsharding.route.orm.base;

import java.lang.reflect.Method;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.util.StringUtils;

import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.db.DataSourceRoutingException;
import com.mogujie.trade.utils.SlowFilterImpl;
import com.mogujie.trade.utils.TshardingFilter;

public class TShardingRoutingInvokeFactory implements InvokerFactory<Class<?>> {
	private SqlSessionFactoryLookup sqlSessionFactoryLookup;
	private TshardingFilter filter = new SlowFilterImpl();

	public TShardingRoutingInvokeFactory(SqlSessionFactoryLookup sqlSessionFactoryLookup) {
		this.sqlSessionFactoryLookup = sqlSessionFactoryLookup;
	}

	@Override
	public Invoker newInvoker(Class<?> mapperInterface) {

		final DataSourceRouting dataSourceRouting = mapperInterface.getAnnotation(DataSourceRouting.class);
		if (dataSourceRouting == null) {
			throw new DataSourceRoutingException("MapperInterface:" + mapperInterface + " 没有找到 @DataSourceRouting");
		}
		if (dataSourceRouting.databases() > 1 || dataSourceRouting.tables() > 1) {
			return newShardingInvoker();
		} else {
			return newSimpleInvoker();
		}
	}

	private Invoker newSimpleInvoker() {
		return new Invoker() {
			@Override
			public Object invoke(Invocation invocation) throws Throwable {
				long start = System.currentTimeMillis();
				MapperBasicConfig config = invocation.getMapperConfig();
				final Object mapper = newMyBatisMapper(config);
				try {
					ReadWriteSplittingContextInitializer.initReadWriteSplittingContext(invocation);
					return invocation.getMethod().invoke(mapper, invocation.getArgs());
				} finally {
					ReadWriteSplittingContextInitializer.clearReadWriteSplittingContext();
					filter.filter(invocation, start, System.currentTimeMillis());
				}
			}
		};
	}

	/**
	 * 创建Sharding
	 */
	private Invoker newShardingInvoker() {
		return new Invoker() {
			@Override
			public Object invoke(Invocation invocation) throws Throwable {
				long start = System.currentTimeMillis();
				MapperBasicConfig config = invocation.getMapperConfig();
				try {
					ReadWriteSplittingContextInitializer.initReadWriteSplittingContext(invocation);
					Method routeMethod = invocation.getRouteMethod();
					final Object mapper = newMyBatisMapper(config);
					return routeMethod.invoke(mapper, invocation.getArgs());
				} finally {
					ReadWriteSplittingContextInitializer.clearReadWriteSplittingContext();
					filter.filter(invocation, start, System.currentTimeMillis());
				}
			}
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object newMyBatisMapper(MapperBasicConfig config) {
		MapperFactoryBean mapperFactoryBean = new MapperFactoryBean();
		mapperFactoryBean.setMapperInterface(config.getShardingMapper());
		mapperFactoryBean.setSqlSessionFactory(
				this.getSqlSessionFactory(config.getDataSourceName(), config.getShardingMapper()));
		mapperFactoryBean.afterPropertiesSet();
		Object mapper = null;
		try {
			mapper = mapperFactoryBean.getObject();
		} catch (Exception e) {
			throw new MapperInitializeException(e);
		}
		return mapper;
	}

	private SqlSessionFactory getSqlSessionFactory(String dataSourceName, Class<?> mapperInterface) {
		if (StringUtils.isEmpty(dataSourceName)) {
			if (sqlSessionFactoryLookup.getMapping().size() == 1) {
				return sqlSessionFactoryLookup.getMapping().values().iterator().next();
			} else {
				throw new DataSourceRoutingException("can't decided the datasource of "
						+ mapperInterface.getCanonicalName() + ",please add config by using @DataSourceRouting");
			}
		} else {
			SqlSessionFactory sqlSessionFactory = sqlSessionFactoryLookup.get(dataSourceName);
			if (sqlSessionFactory == null) {
				throw new DataSourceRoutingException("can't find dataSource:`" + dataSourceName + "`");
			}
			return sqlSessionFactory;
		}
	}

}

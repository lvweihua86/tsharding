package com.mogujie.distributed.transction;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.intercept.Interceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.utils.TransactionManagerUtils.TransactionProxy;

/**
 * 链式事物管理拦截器
 * 
 * @author SHOUSHEN LUAN
 *
 */
@Aspect
@Component
public class ChainedTransactionInteceptor implements Interceptor {
	@Autowired
	private DynamicTransctionManagerFactory dtmFactory;
	private final Map<String, Entity> cacheMap = new ConcurrentHashMap<>();

	@Around("@annotation(com.mogujie.distributed.transction.ChainedTransaction)")
	public Object invoke(ProceedingJoinPoint pjp) throws Throwable {
		if (!cacheMap.containsKey(pjp.toLongString())) {
			cacheMap.put(pjp.toLongString(), new Entity(pjp));
		}
		Entity entity = cacheMap.get(pjp.toLongString());
		Class<?>[] mappers = entity.getMapper();
		DynamicTransctionManager transctionManager = dtmFactory.create();
		for (Class<?> mapper : mappers) {
			DataSourceRouting routing = mapper.getAnnotation(DataSourceRouting.class);
			if (routing.databases() > 1) {
				List<Object> params = FieldUtils.parserParam(mapper, entity, pjp.getArgs());
				if (params == null || params.size() == 0) {
					throw new IllegalArgumentException(
							"mapper:`" + mapper + "` ShardingParam must not empty. use @DataSourceRouting("
									+ mapper.getSimpleName() + "...)");
				}
				transctionManager.addTransManager(mapper, params);
			} else {
				transctionManager.addTransManager(mapper);
			}
		}
		TransactionProxy transactionProxy = transctionManager.build(entity.getTimeout());
		try {
			Object result = pjp.proceed(pjp.getArgs());
			transactionProxy.commit();
			return result;
		} catch (Throwable e) {
			if (entity.isRollback(e)) {
				transactionProxy.rollback();
			} else {
				transactionProxy.commit();
			}
			throw e;
		}
	}

	class Entity {
		private final String staticPart;
		final Method method;
		final ChainedTransaction transaction;
		final Map<Class<?>, Integer> mapper_paramIndex = new HashMap<>();
		private List<Class<?>> shardingMappers = new ArrayList<>();

		public Entity(ProceedingJoinPoint pjp) {
			staticPart = pjp.toLongString();
			this.method = ((MethodSignature) pjp.getSignature()).getMethod();
			this.transaction = method.getAnnotation(ChainedTransaction.class);
			check();
		}

		public Entity(Method method, String staticPart) {
			this.staticPart = staticPart;
			this.method = method;
			this.transaction = method.getAnnotation(ChainedTransaction.class);
			check();
		}

		public RouteParam getRouteParam(Class<?> mapper) {
			int index = this.getParamIndex(mapper);
			Parameter parameter = method.getParameters()[index];
			RouteParam routeParam = parameter.getAnnotation(RouteParam.class);
			if (routeParam == null) {
				throw new IllegalArgumentException(staticPart + "注解mapper:`" + mapper + "`没有找到映射@RouteParam");
			}
			return routeParam;
		}

		public int getParamIndex(Class<?> mapper) {
			if (mapper_paramIndex.containsKey(mapper)) {
				return mapper_paramIndex.get(mapper);
			} else {
				throw new IllegalArgumentException(staticPart + "没有找到Mapper:`" + mapper + "`");
			}
		}

		public boolean isRollback(Throwable e) {
			Class<?> clazzs[] = transaction.rollbackFor();
			for (int i = 0; i < clazzs.length; i++) {
				if (clazzs[i].isAssignableFrom(e.getClass())) {
					return true;
				}
			}
			return false;
		}

		public Class<?>[] getMapper() {
			return transaction.mapper();
		}

		public int getTimeout() {
			return transaction.timeout();
		}

		private void check() {
			Class<?>[] mappers = transaction.mapper();
			if (mappers == null || mappers.length == 0) {
				throw new IllegalArgumentException(staticPart + ".mappers must not empty");
			}
			for (Class<?> mapper : mappers) {
				DataSourceRouting routing = mapper.getAnnotation(DataSourceRouting.class);
				if (routing == null) {
					throw new IllegalArgumentException(staticPart + ".无效的Mapper:" + mapper);
				}
				if (routing.databases() > 1) {
					shardingMappers.add(mapper);
				}
			}
			Parameter[] parameters = method.getParameters();
			for (int i = 0; i < parameters.length; i++) {
				RouteParam routeParam = parameters[i].getAnnotation(RouteParam.class);
				if (routeParam != null) {
					Class<?> mapper = loadShardingMapperAndRemove(routeParam.value());
					mapper_paramIndex.put(mapper, i);
				}
			}
			if (shardingMappers.size() > 0) {
				throw new IllegalArgumentException(staticPart + ".Mapper:`" + Arrays.toString(shardingMappers.toArray())
						+ "` missing @RouteParam(...)");
			}
		}

		private Class<?> loadShardingMapperAndRemove(String expression) {
			String name = getMapperName(expression);
			for (Class<?> clazz : shardingMappers) {
				if (clazz.getSimpleName().equals(name)) {
					shardingMappers.remove(clazz);
					return clazz;
				}
			}
			throw new IllegalArgumentException(staticPart + ".invalid @RouteParam(value='" + name + "')");
		}

		private String getMapperName(String str) {
			int index = str.indexOf(".");
			if (index == -1) {
				return str;
			} else {
				return str.substring(0, index);
			}
		}
	}
}

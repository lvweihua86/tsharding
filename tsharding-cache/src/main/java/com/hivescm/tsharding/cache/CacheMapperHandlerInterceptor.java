package com.hivescm.tsharding.cache;

import org.springframework.beans.factory.annotation.Autowired;

import com.hivescm.cache.client.JedisClient;
import com.hivescm.cache.utils.CacheLogger;
import com.hivescm.tsharding.cache.annotation.MapperCached;
import com.hivescm.tsharding.utils.MapperUtils;
import com.mogujie.trade.utils.TShardingLog;
import com.mogujie.tsharding.filter.InvocationProxy;
import com.mogujie.tsharding.filter.MapperHandlerInterceptor;

/**
 * 支持缓存处理的拦截器
 * 
 * @author SHOUSHEN LUAN
 *
 */
public class CacheMapperHandlerInterceptor implements MapperHandlerInterceptor {
	@Autowired
	private JedisClient<Object> jedisClient;

	@Override
	public Object invoker(InvocationProxy invocation) throws Throwable {
		MapperHander mapperHander = MapperUtils.getMapperHander(invocation);
		if (mapperHander.hasCache()) {
			return useCacheProcess(invocation, mapperHander);
		} else if (mapperHander.hasCacheEvicted()) {
			try {
				return this.doInvoker(invocation, mapperHander);
			} finally {
				clearCache(mapperHander);
			}
		} else {
			return this.doInvoker(invocation, mapperHander);
		}
	}

	/**
	 * 执行DB操作
	 * 
	 * @param invocation
	 * @param mapperHander
	 * @return
	 * @throws Throwable
	 */
	private Object doInvoker(InvocationProxy invocation, MapperHander mapperHander) throws Throwable {
		long start = System.currentTimeMillis();
		try {
			return invocation.doInvoker();
		} finally {
			long useTime = System.currentTimeMillis() - start;
			// 每次DB操作均记录日志，日后可以根据开关控制
			TShardingLog.getLogger().info(mapperHander.getLogInfo(useTime));
		}
	}

	/**
	 * 清楚缓存处理
	 * 
	 * @param mapperHander
	 * @param args
	 * @throws Throwable
	 */
	private void clearCache(MapperHander mapperHander) throws Throwable {
		try {
			String keys[] = mapperHander.markCacheEvincted();
			jedisClient.delete(keys);
		} catch (Exception e) {
			CacheLogger.getLogger().error("clearCache", e);
		}
	}

	private Object useCacheProcess(InvocationProxy invocation, MapperHander mapperHander) throws Throwable {
		String key = mapperHander.markCacheKey();
		Object value = jedisClient.getPojo(key);
		if (value != null) {
			if (MapperCached.EMPTY.equals(value)) {
				return null;// cache null value
			} else {
				return value;
			}
		}
		value = this.doInvoker(invocation, mapperHander);
		setCache(mapperHander, key, value);
		return value;
	}

	/**
	 * 设置缓存
	 * 
	 * @param mapperHander
	 * @param key
	 * @param value
	 */
	private boolean setCache(MapperHander mapperHander, String key, Object value) {
		try {
			MapperCached tshardingCached = mapperHander.getCache();
			if (value == null) {
				if (tshardingCached.cacheNull()) {
					return jedisClient.setPojo(key, MapperCached.EMPTY, tshardingCached.expire());
				}
			} else {
				return jedisClient.setPojo(key, value, tshardingCached.expire());
			}
		} catch (Exception e) {
			CacheLogger.getLogger().error("setCache ERROR", e);
		}
		return false;
	}

}

package com.hivescm.tsharding.cache;

import org.springframework.beans.factory.annotation.Autowired;

import com.hivescm.cache.client.JedisClient;
import com.hivescm.cache.utils.RedisLogger;
import com.hivescm.tsharding.utils.MapperUtils;
import com.mogujie.trade.utils.TShardingLog;
import com.mogujie.tsharding.cache.TshardingCached;
import com.mogujie.tsharding.filter.InvocationProxy;
import com.mogujie.tsharding.filter.MapperHandlerInterceptor;

/**
 * 支持缓存处理的拦截器
 * 
 * @author kevin
 *
 */
public class CacheMapperHandlerInterceptor implements MapperHandlerInterceptor {
	@Autowired
	private JedisClient<Object> jedisClient;

	@Override
	public Object invoker(InvocationProxy invocation) throws Throwable {
		long start = System.currentTimeMillis();
		MapperHander mapperHander = MapperUtils.getMapperHander(invocation);
		try {
			if (mapperHander.hasTsharingCache()) {
				return useCacheProcess(invocation, mapperHander);
			} else if (mapperHander.hasCacheEvicted()) {
				try {
					return invocation.doInvoker();
				} finally {
					clearCache(mapperHander);
				}
			} else {
				return invocation.doInvoker();
			}
		} finally {
			long useTime = System.currentTimeMillis() - start;
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
			String key = mapperHander.markCacheEvincted();
			jedisClient.delete(key);
		} catch (Exception e) {
			RedisLogger.getLogger().error("clearCache", e);
		}
	}

	private Object useCacheProcess(InvocationProxy invocation, MapperHander mapperHander) throws Throwable {
		String key = mapperHander.markCacheKey();
		Object value = jedisClient.getPojo(key);
		if (value != null) {
			if (TshardingCached.EMPTY.equals(value)) {
				return null;// cache null value
			} else {
				return value;
			}
		}
		value = invocation.doInvoker();
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
	private void setCache(MapperHander mapperHander, String key, Object value) {
		try {
			TshardingCached tshardingCached = mapperHander.getCache();
			if (value == null) {
				if (tshardingCached.cacheNull()) {
					jedisClient.setPojo(key, value, tshardingCached.expire());
				}
			} else {
				jedisClient.setPojo(key, value, tshardingCached.expire());
			}
		} catch (Exception e) {
			RedisLogger.getLogger().error("setCache ERROR", e);
		}
	}

}

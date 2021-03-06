package com.hivescm.tsharding.cache;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hivescm.cache.annotation.CacheEvicted;
import com.hivescm.cache.annotation.Cached;
import com.hivescm.cache.annotation.GroupCacheEvicted;
import com.hivescm.cache.aspect.MethodAspectInteceptor;
import com.mogujie.trade.tsharding.route.orm.base.Invocation;
import com.mogujie.tsharding.filter.InvocationProxy;

@Component
public class ProxyCacheHandler {

	@Autowired
	private MethodAspectInteceptor methodAspectInteceptor;

	public boolean hasCached(Method method) {
		return method.getAnnotation(Cached.class) != null;
	}

	public boolean hasCacheEvicted(Method method) {
		return method.getAnnotation(CacheEvicted.class) != null;
	}

	public boolean hasGroupCacheEvicted(Method method) {
		return method.getAnnotation(GroupCacheEvicted.class) != null;
	}

	public Object invoke(InvocationProxy invocationProxy, CacheMapperHandlerInterceptor interceptor) throws Throwable {
		ProceedingJoinPoint pjp = createProceedingJoinPoint(invocationProxy, interceptor);
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		try {
			if (hasCacheEvicted(method)) {
				methodAspectInteceptor.before(pjp);
			}
			Object result = null;
			if (hasCached(method)) {
				result = methodAspectInteceptor.invoke_jedis_cache(pjp);
			} else {
				result = invocationProxy.doInvoker();
			}
			return result;
		} finally {
			if (hasGroupCacheEvicted(method)) {
				methodAspectInteceptor.After(pjp);
			}
		}
	}

	private ProceedingJoinPoint createProceedingJoinPoint(InvocationProxy invocationProxy,
			CacheMapperHandlerInterceptor interceptor) {
		Invocation invocation = invocationProxy.getInvocation();
		Method method;
		Class<?> targetClass;
		if (invocation.getDataSourceRouting().tables() > 1 || invocation.getDataSourceRouting().databases() > 1) {
			method = invocation.getRouteMethod();
			targetClass = invocation.getShardingMappingClass();
		} else {
			method = invocation.getMethod();
			targetClass = invocation.getMapperClass();
		}
		Object[] arguments = invocation.getArgs();
		ProxyCacheMethodInvocation pcmi = new ProxyCacheMethodInvocation(invocationProxy, method, arguments, targetClass,
				interceptor);
		return new MethodInvocationProceedingJoinPoint(pcmi);
	}
}

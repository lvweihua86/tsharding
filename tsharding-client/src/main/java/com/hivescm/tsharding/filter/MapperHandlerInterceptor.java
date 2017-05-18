package com.hivescm.tsharding.filter;

public interface MapperHandlerInterceptor {
	public Object invoker(InvocationProxy invocation) throws Throwable;
}

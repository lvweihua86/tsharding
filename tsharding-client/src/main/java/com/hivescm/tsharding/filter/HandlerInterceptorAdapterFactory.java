package com.hivescm.tsharding.filter;

public class HandlerInterceptorAdapterFactory {
	private static MapperHandlerInterceptor interceptor;

	/**
	 * 使用者自行扩展拦截器
	 * 
	 * @param interceptor
	 */
	public HandlerInterceptorAdapterFactory(MapperHandlerInterceptor interceptor) {
		HandlerInterceptorAdapterFactory.interceptor = interceptor;
	}

	public static Object doInvoker(InvocationProxy invocation) throws Throwable {
		if (interceptor != null) {
			return interceptor.invoker(invocation);
		} else {
			return invocation.doInvoker();
		}
	}
}

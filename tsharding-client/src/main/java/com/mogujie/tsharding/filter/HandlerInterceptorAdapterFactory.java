package com.mogujie.tsharding.filter;

public class HandlerInterceptorAdapterFactory {
	private static volatile MapperHandlerInterceptor interceptor=new SimpleMapperHandlerInterceptor();

	/**
	 * 使用者自行扩展拦截器
	 * 
	 * @param interceptor
	 */
	public HandlerInterceptorAdapterFactory(MapperHandlerInterceptor interceptor) {
		HandlerInterceptorAdapterFactory.interceptor = interceptor;
	}

	/**
	 * 注入拦截器
	 * 
	 * @param interceptor
	 */
	public static void registerInterceptor(MapperHandlerInterceptor interceptor) {
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

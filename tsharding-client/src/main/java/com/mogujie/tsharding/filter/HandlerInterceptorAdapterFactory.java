package com.mogujie.tsharding.filter;

public class HandlerInterceptorAdapterFactory {
	private static MapperHandlerInterceptor interceptor = new SimpleMapperHandlerInterceptor();

	/**
	 * 注入拦截器
	 * 
	 * @param interceptor
	 */
	public static void registerInterceptor(MapperHandlerInterceptor interceptor) {
		System.out.println("注册拦截器:" + interceptor);
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

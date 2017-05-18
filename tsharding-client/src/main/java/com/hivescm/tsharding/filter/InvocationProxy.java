package com.hivescm.tsharding.filter;

import com.mogujie.trade.tsharding.route.orm.base.Invocation;

public interface InvocationProxy {
	public Invocation getInvoker();

	public Object doInvoker() throws Throwable;
}

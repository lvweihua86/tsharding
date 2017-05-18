package com.hivescm.tsharding.filter;

import com.mogujie.trade.tsharding.route.orm.base.Invocation;

public abstract class AbstraceInvocation implements InvocationProxy {
	private Invocation invocation;

	public AbstraceInvocation(Invocation invocation) {
		this.invocation = invocation;
	}

	@Override
	public Invocation getInvoker() {
		return invocation;
	}
}

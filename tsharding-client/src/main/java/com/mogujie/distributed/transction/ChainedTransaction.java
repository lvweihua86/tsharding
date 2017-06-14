package com.mogujie.distributed.transction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.TransactionDefinition;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChainedTransaction {
	/**
	 * 定义事物Mapper(根据Mapper定义选择开启事物管理器)
	 *
	 * @return
	 */
	public Class<?>[] mapper();

	/**
	 * The timeout for this transaction. Defaults to the default timeout of the
	 * underlying transaction system.
	 * 
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
	 */
	int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

	/**
	 * 定义回滚异常类 默认 AnyException (任意异常)回退
	 * 
	 * @return
	 */
	Class<? extends Throwable>[] rollbackFor() default { AnyException.class };
}

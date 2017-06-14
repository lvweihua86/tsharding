package com.mogujie.distributed.transction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.RollbackException;

import org.springframework.transaction.TransactionDefinition;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChainedTransaction {
	/**
	 * 定义事物Mapper
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
	 * 默认 RollbackException 异常回退
	 * 
	 * @return
	 */
	Class<? extends Throwable>[] rollbackFor() default { RollbackException.class };
}

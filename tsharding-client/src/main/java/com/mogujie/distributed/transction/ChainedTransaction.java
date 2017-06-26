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
	 * <p>
	 * 注意最后提交事物的顺序是按照Mapper定义顺序来提交的
	 * </p>
	 * <ul>
	 * <li>
	 * 转账示例：先扣款在加款的场景中，需要顺序如下={SubtractMoneyMapper.class,AddMoneyMapper.class}
	 * </li>
	 * <li>提交顺序按照从前到后依次提交，如果前面的事物提交出错，则后面的事物会触发回滚操作。</li>
	 * <li>假如扣款事物提交失败，则加款事物会自动进行回滚操作。</li>
	 * </ul>
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

	/**
	 * 出现未完成的事物提交或退滚回调方法
	 * <ul>
	 * <li>默认使用与当前方法名称+_Callback作为回调方法</li>
	 * <li>例如: 方法名称:testAbc 在出现未完成的异常时，会调用testAbc_Callback(ProxyMethodpm)</li>
	 * </ul>
	 * 
	 * @return
	 */
	String unfinishedCallback() default "";
}

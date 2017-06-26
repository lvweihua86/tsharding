package com.mogujie.distributed.transction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.mogujie.route.rule.RouteRule;
import com.mogujie.route.rule.RouteRuleFactory;
import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.utils.TransactionManagerUtils;
import com.mogujie.trade.utils.TransactionManagerUtils.TransactionProxy;

/**
 * 动态事物管理器
 * 
 * @author SHOUSHEN LUAN
 *
 */
public class DynamicTransctionManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTransctionManager.class);
	private ApplicationContext applicationContext;
	private Vector<String> transManagers = new Vector<>();
	private PlatformTransactionManager[] transactionManagers;

	protected DynamicTransctionManager(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public TransactionProxy build() {
		return build(-1);
	}

	/**
	 * 生成事物管理器
	 * 
	 * @param timeout
	 *            unit second
	 * @return
	 */
	public TransactionProxy build(int timeout) {
		return TransactionManagerUtils.createTransaction(createChainedTransactionManager(), timeout);
	}

	/**
	 * 添加事物管理器
	 * 
	 * @param routeRule
	 * @param routing
	 * @param shardingParam
	 * @return
	 */
	public DynamicTransctionManager addTransManager(Class<?> mapper, Object... shardingParams) {
		if (shardingParams == null || shardingParams.length == 0) {
			throw new IllegalArgumentException("缺少shardingParams");
		}
		RouteRule<Object> routeRule = RouteRuleFactory.getRouteRule(mapper);
		DataSourceRouting routing = mapper.getAnnotation(DataSourceRouting.class);
		if (routing.tables() > 1 && routing.databases() > 1) {
			for (Object param : shardingParams) {
				addTransManager(routeRule, routing, param);
			}
		} else {
			throw new IllegalArgumentException("不支持Sharding参数的Mapper:" + mapper);
		}
		return this;
	}

	public DynamicTransctionManager addTransManager(Class<?> mapper, List<Object> params) {
		if (params == null || params.size() == 0) {
			throw new IllegalArgumentException("params must not empty");
		}
		RouteRule<Object> routeRule = RouteRuleFactory.getRouteRule(mapper);
		DataSourceRouting routing = mapper.getAnnotation(DataSourceRouting.class);
		if (routing.tables() > 1 && routing.databases() > 1) {
			for (Object param : params) {
				addTransManager(routeRule, routing, param);
			}
		} else {
			throw new IllegalArgumentException("不支持Sharding参数的Mapper:" + mapper);
		}
		return this;
	}

	private boolean addTransManager(RouteRule<Object> routeRule, DataSourceRouting routing, Object shardingParam) {
		String dataSource;
		if (routing.tables() > 1 && routing.databases() > 1) {
			dataSource = routeRule.calculateSchemaName(routing, shardingParam);
		} else {
			dataSource = routing.dataSource();
		}
		return addTransManager(dataSource);
	}

	private boolean addTransManager(String dataSource) {
		String name = dataSource + "TransactionManager";
		if (transManagers.contains(name)) {
			return false;
		} else {
			return transManagers.add(name);
		}
	}

	/**
	 * 添加事物管理器
	 * 
	 * @param routeRule
	 * @param routing
	 * @param shardingParam
	 * @return
	 */
	public DynamicTransctionManager addTransManager(Class<?> mapper) {
		DataSourceRouting routing = mapper.getAnnotation(DataSourceRouting.class);
		String dataSource = routing.dataSource();
		if (routing.tables() > 1 || routing.databases() > 1) {
			throw new IllegalArgumentException(
					"mapper:" + mapper + " 请使用 addTransManager(Class<?> mapper, Object... shardingParams)");
		}
		addTransManager(dataSource);
		return this;
	}

	private ChainedTransactionManager createChainedTransactionManager() {
		transactionManagers = new PlatformTransactionManager[transManagers.size()];
		int index=0;
		for (int i = transManagers.size() - 1; i >= 0; i--) {
			String name= transManagers.get(i);
			PlatformTransactionManager ptm = applicationContext.getBean(name, PlatformTransactionManager.class);
			transactionManagers[index] = ptm;
			index++;
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("dynamic create chained transction manager:" + Arrays.toString(transManagers.toArray()));
		}
		return new ChainedTransactionManager(transactionManagers);
	}
}

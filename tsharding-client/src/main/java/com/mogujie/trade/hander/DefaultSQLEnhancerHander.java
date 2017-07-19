package com.mogujie.trade.hander;

import com.mogujie.route.rule.RouteRuleFactory;
import com.mogujie.trade.db.DataSourceRouting;

public class DefaultSQLEnhancerHander implements SQLEnhancerHander {
	protected Class<?> mappedClass;
	private DataSourceRouting routing;

	public DefaultSQLEnhancerHander(Class<?> mappedClass) {
		this.mappedClass = mappedClass;
		this.routing = mappedClass.getAnnotation(DataSourceRouting.class);
	}

	@Override
	public String getTable(long value) {
		if (routing.tables() > 1) {
			String table = routing.table();
			String tableSuffix = RouteRuleFactory.getRouteRule(mappedClass).fillBit(value);
			return table + tableSuffix;
		} else {
			return routing.table();
		}
	}

	@Override
	public boolean hasReplace(String sql) {
		return sql.contains(routing.table());
	}

	@Override
	public String format(String sql, long value) {
		sql = format(sql);
		String newTableName = getTable(value);
		sql = sql.replace(routing.table(), newTableName);
		return sql;
	}

	/**
	 * 格式化SQL语句
	 * 
	 * @param sql
	 * @return
	 */
	private String format(String sql) {
		if (sql != null) {
			sql = sql.replace('\n', ' ');
			sql = sql.replace('\t', ' ');
			sql = sql.replace("  ", " ");
		}
		return sql;
	}
}

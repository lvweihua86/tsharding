package com.mogujie.trade.hander;

/**
 * SQL增强处理器
 * @CreateTime 2016年8月6日 上午9:45:06
 * @author SHOUSHEN LUAN
 */
public interface SQLEnhancerHander {
	/**
	 * 获取目标表
	 * @param value 分表后缀值.范围:0~tableN-1
	 */
	public String getTable(long value);

	/**
	 * 是否存在需要替换的表名称
	 */
	public boolean hasReplace(String sql);

	/**
	 * 修改SQL中的表名称
	 */
	public String format(String text, long value);
}

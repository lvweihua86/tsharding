package com.mogujie.service.tsharding.dao;

import com.mogujie.service.tsharding.bean.ShopOrder;

import java.util.List;

/**
 * @auther qigong on 6/5/15 8:50 PM.
 */
public interface ShopOrderDao {

	/**
	 * 根据店铺级订单ID获取订单信息（同一个买家）
	 *
	 * @param listShopOrderIds
	 *            店铺级订单ID集合
	 * @return List<XdShopOrder>
	 */
	List<ShopOrder> getShopOrderByShopOrderIds(List<Long> listShopOrderIds);

	public boolean insert(ShopOrder order);

	/**
	 * 插入数据出错不会滚
	 */
	public boolean insert_err_no_rollback(ShopOrder order);

	/**
	 * 插入数据出错回滚
	 */
	public boolean insert_rollback(ShopOrder order);

	/**
	 * 测试编程式分布式事物
	 * 
	 * @param order
	 * @param isCommit
	 * @return
	 */
	public boolean programmeTransaction(ShopOrder order, boolean isCommit);

	/**
	 * 测试编程式分布式事物
	 * 
	 * @param order
	 * @param isCommit
	 * @return
	 */
	public boolean chainedTransaction(ShopOrder order, boolean isCommit);

	public boolean test_NoShardingParamErr();

	public boolean test_NoShardingParam();

	public boolean testShardingPojoList(List<ShopOrder> list);

	public boolean testErrorShardingParam(String str, List<ShopOrder> list);

	public boolean testShardingNumList(int num, List<Long> list);
}

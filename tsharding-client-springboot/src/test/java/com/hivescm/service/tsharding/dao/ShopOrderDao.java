package com.hivescm.service.tsharding.dao;

import com.mogujie.service.tsharding.bean.ShopOrder;

import java.util.List;

/**
 * @auther qigong on 6/5/15 8:50 PM.
 */
public interface ShopOrderDao {

    /**
     * 根据店铺级订单ID获取订单信息（同一个买家）
     *
     * @param listShopOrderIds 店铺级订单ID集合
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
}

package com.mogujie.service.tsharding.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mogujie.service.tsharding.bean.ShopOrder;
import com.mogujie.service.tsharding.mapper.ShopOrderMapper;

/**
 * @auther qigong on 6/5/15 8:52 PM.
 */
@Service("shopOrderDao")
public class ShopOrderDaoImpl implements ShopOrderDao {

    @Autowired
    private ShopOrderMapper shopOrderMapper;

    @Override
    public List<ShopOrder> getShopOrderByShopOrderIds(List<Long> listShopOrderIds) {
        if (listShopOrderIds == null || listShopOrderIds.size() == 0) {
            return null;
        }
        Set<Long> setShopOrderIds = new HashSet<Long>();
        for (Long iShopOrderId : listShopOrderIds) {
            if (iShopOrderId > 0) {
                setShopOrderIds.add(iShopOrderId);
            }
        }
        return shopOrderMapper.getShopOrderByShopOrderIds(new ArrayList<Long>(setShopOrderIds));
    }

    @Transactional(value = "tradeTransactionManager", rollbackFor = IllegalArgumentException.class, noRollbackFor = RuntimeException.class)
    public boolean insert_rollback(ShopOrder order) {
        int result = shopOrderMapper.insertOrder(order);
        System.out.println("回滚处理" + result);
        throw new IllegalArgumentException("回滚异常");
    }

    @Transactional(value = "tradeTransactionManager", rollbackFor = IllegalArgumentException.class, noRollbackFor = RuntimeException.class)
    public boolean insert_err_no_rollback(ShopOrder order) {
        int result = shopOrderMapper.insertOrder(order);
        System.out.println("不回滚" + result);
        throw new RuntimeException("不回滚异常");
    }

    @Transactional(value = "tradeTransactionManager", rollbackFor = IllegalArgumentException.class, noRollbackFor = RuntimeException.class)
    public boolean insert(ShopOrder order) {
        int result = shopOrderMapper.insertOrder(order);
        return result >= 0;
    }

}

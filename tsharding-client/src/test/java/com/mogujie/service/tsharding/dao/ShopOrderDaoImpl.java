package com.mogujie.service.tsharding.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mogujie.distributed.transction.DynamicTransctionManagerFactory;
import com.mogujie.service.tsharding.bean.Product;
import com.mogujie.service.tsharding.bean.ShopOrder;
import com.mogujie.service.tsharding.mapper.ProductMapper;
import com.mogujie.service.tsharding.mapper.ShopOrderMapper;
import com.mogujie.trade.utils.TransactionManagerUtils.TransactionProxy;

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

	@Autowired
	private DynamicTransctionManagerFactory dtmFactory;
	@Autowired
	private ProductMapper productMapper;

	public boolean programmeTransaction(ShopOrder order, boolean isCommit) {
		// 开启事物管理器
		TransactionProxy transactionProxy = dtmFactory.create()//
				.addTransManager(ProductMapper.class)//
				.addTransManager(ShopOrderMapper.class, order.getOrderId())//
				.build(2);
		// 插入订单(订单表进行了分库分表)
		shopOrderMapper.insertOrder(order);
		// 获取商品
		Product product = productMapper.getByName("Mac book");
		// 删除商品
		productMapper.delete(product.getId());
		// TODO 现实中会根据自身业务逻辑决定是提交还是回退
		if (isCommit) {
			transactionProxy.commit();
		} else {
			transactionProxy.rollback();
		}
		return true;
	}

}

package com.mogujie.service.tsharding.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.pattern.EncodingPatternConverter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mogujie.service.tsharding.bean.ShopOrder;
import com.mogujie.service.tsharding.bean.UserInfo;
import com.mogujie.service.tsharding.dao.ShopOrderDao;
import com.mogujie.service.tsharding.mapper.ShopOrderMapper;
import com.mogujie.service.tsharding.mapper.UserInfoMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:spring-tsharding.xml" })
public class OtherTest {
	@Autowired
	private UserInfoMapper userInfoMapper;

	@Test
	public void test() {
		for (int i = 0; i < 10; i++) {
			UserInfo info = new UserInfo();
			info.setAge(1);
			info.setName(i + "----" + i);
			info.setNickName("xx");
			info.setSex(1);
			int res = userInfoMapper.insert(info);
			Assert.assertEquals(1, res);
		}
		for (int i = 0; i < 10; i++) {
			UserInfo user = userInfoMapper.getByName(i + "----" + i);
			Assert.assertTrue(user != null);
			int res = userInfoMapper.delete(user.getId());
			Assert.assertEquals(1, res);
		}
	}

	@Test
	public void test_cache() {
		for (int i = 0; i < 1000; i++) {
			userInfoMapper.get(i);
		}
	}

	@Autowired
	private ShopOrderDao shopOrderDao;

	@Test
	public void testDistributedTransction() {
		for (int i = 0; i < 5; i++) {
			long start = System.currentTimeMillis();
			ShopOrder order6 = new ShopOrder();
			order6.setOrderId(6L);
			order6.setBuyerUserId(1006L);
			order6.setSellerUserId(10086L);
			order6.setShipTime(System.currentTimeMillis());
			shopOrderDao.programmeTransaction(order6, i % 2 == 0);
			System.out.println("use time:" + (System.currentTimeMillis() - start));
		}
	}

	@Test
	public void testChainedTransaction() {
		for (int i = 0; i < 5; i++) {
			long start = System.currentTimeMillis();
			try {
				ShopOrder order6 = new ShopOrder();
				order6.setOrderId(6L);
				order6.setBuyerUserId(1006L);
				order6.setSellerUserId(10086L);
				order6.setShipTime(System.currentTimeMillis());
				shopOrderDao.chainedTransaction(order6, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("use time:" + (System.currentTimeMillis() - start));
		}
	}

	@Test
	public void test_NoShardingParam() {
		Assert.assertTrue(shopOrderDao.test_NoShardingParam());
	}

	@Test
	public void test_NoShardingParamErr() {
		try {
			shopOrderDao.test_NoShardingParamErr();
			Assert.assertTrue(false);
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testErrorShardingParam() {
		List<ShopOrder> list = new ArrayList<>();
		ShopOrder order6 = new ShopOrder();
		order6.setOrderId(6L);
		order6.setBuyerUserId(1006L);
		order6.setSellerUserId(10086L);
		order6.setShipTime(System.currentTimeMillis());
		list.add(order6);
		ShopOrder order7 = new ShopOrder();
		order6.setOrderId(7L);
		order6.setBuyerUserId(1006L);
		order6.setSellerUserId(10086L);
		order6.setShipTime(System.currentTimeMillis());
		list.add(order7);
		try {
			shopOrderDao.testErrorShardingParam("x", list);
			Assert.assertTrue(false);
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testShardingPojoList() {
		List<ShopOrder> list = new ArrayList<>();
		ShopOrder order6 = new ShopOrder();
		order6.setOrderId(6L);
		order6.setBuyerUserId(1006L);
		order6.setSellerUserId(10086L);
		order6.setShipTime(System.currentTimeMillis());
		list.add(order6);
		ShopOrder order7 = new ShopOrder();
		order7.setOrderId(7L);
		order7.setBuyerUserId(1006L);
		order7.setSellerUserId(10086L);
		order7.setShipTime(System.currentTimeMillis());
		list.add(order7);
		shopOrderDao.testShardingPojoList(list);
	}

	@Test
	public void testShardingNumList() {
		List<Long> list = new ArrayList<>();
		list.add(1L);
		list.add(2L);
		list.add(3L);
		list.add(4L);
		list.add(5L);
		shopOrderDao.testShardingNumList(1, list);
	}
}

package com.mogujie.service.tsharding.test;

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
		for (int i = 0; i < 10000; i++) {
			long start = System.currentTimeMillis();
			ShopOrder order6 = new ShopOrder();
			order6.setOrderId(6L);
			order6.setBuyerUserId(1006L);
			order6.setSellerUserId(10086L);
			order6.setShipTime(System.currentTimeMillis());
			shopOrderDao.programmeTransaction(order6, true);
			System.out.println("use time:" + (System.currentTimeMillis() - start));
		}
	}
}

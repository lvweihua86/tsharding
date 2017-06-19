package com.hivescm.cache.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hivescm.common.conf.SystemManager;
import com.mogujie.service.tsharding.bean.UserInfo;
import com.mogujie.service.tsharding.mapper.UserInfoMapper;

/**
 * 单数据库
 * 
 * @author SHOUSHEN LUAN
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:spring-tsharding.xml" })
public class OneDatabaseTest {
	@Autowired
	private UserInfoMapper userInfoMapper;
	static {
		SystemManager.project_Name = "tsharding-cache";
	}

	@Test
	public void insert() {
		for (int i = 0; i < 100; i++) {
			long start = System.currentTimeMillis();
			UserInfo userInfo = new UserInfo();
			userInfo.setName("kevin");
			userInfo.setAge(10);
			userInfo.setSex(1);
			int res = userInfoMapper.insert(userInfo);
			Assert.assertTrue(res == 1);
			userInfo = userInfoMapper.getByName("kevin");

			System.out.println(userInfoMapper.get(userInfo.getId()));
			System.out.println(userInfoMapper.get(userInfo.getId()));

			Assert.assertEquals(1, userInfoMapper.delete(userInfo.getId()));
			Assert.assertEquals(null, userInfoMapper.get(userInfo.getId()));
			Assert.assertEquals(null, userInfoMapper.get(userInfo.getId()));
			System.out.println("\tinsert use time:" + (System.currentTimeMillis() - start));
		}
	}

}

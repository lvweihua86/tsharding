package com.hivescm.service.tsharding.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hivescm.service.tsharding.bean.UserInfo;
import com.hivescm.service.tsharding.mapper.UserInfoMapper;
import com.hivescm.tsharding.startup.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
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
}

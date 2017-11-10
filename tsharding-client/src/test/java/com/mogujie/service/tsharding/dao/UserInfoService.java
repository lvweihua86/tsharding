package com.mogujie.service.tsharding.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import com.mogujie.distributed.transction.ChainedTransaction;
import com.mogujie.service.tsharding.bean.UserInfo;
import com.mogujie.service.tsharding.mapper.UserInfoMapper;

@Component
public class UserInfoService {
	@Autowired
	private UserInfoMapper userInfoMapper;

	@ChainedTransaction(mapper = { UserInfoMapper.class }, timeout = 2, propagation = Propagation.REQUIRES_NEW)
	public UserInfo test_ChainedTransaction(boolean succ, String name) {
		UserInfo userInfo = new UserInfo();
		userInfo.setName(name);
		userInfo.setAge(100);
		userInfo.setNickName("hello kitty");
		userInfo.setSex(1);
		userInfoMapper.insert(userInfo);
		if (succ && userInfo.getId() > 0) {
			return userInfo;
		}
		throw new RuntimeException("数据回滚");
	}

	@ChainedTransaction(mapper = { UserInfoMapper.class })
	public UserInfo test_del(boolean succ, String name) {
		UserInfo userInfo = userInfoMapper.getByName(name);
		int res = userInfoMapper.delete(userInfo.getId());
		if (succ && res > 0) {
			return userInfo;
		}
		throw new RuntimeException("数据回滚");
	}

	/**
	 * 测试当前事物必须在一个已经存在的事物中执行
	 * 
	 * @param succ
	 * @param name
	 * @return
	 */
	@ChainedTransaction(mapper = { UserInfoMapper.class }, propagation = Propagation.MANDATORY)
	public UserInfo test_Transaction_MANDATORY(String name) {
		UserInfo userInfo = new UserInfo();
		userInfo.setName(name);
		userInfo.setAge(100);
		userInfo.setNickName("hello kitty");
		userInfo.setSex(1);
		userInfoMapper.insert(userInfo);
		return userInfo;
	}
}

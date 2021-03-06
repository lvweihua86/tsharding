package com.mogujie.service.tsharding.mapper;

import com.hivescm.cache.annotation.CacheEvicted;
import com.hivescm.cache.annotation.Cached;
import com.hivescm.cache.annotation.GroupCacheEvicted;
import com.mogujie.service.tsharding.bean.UserInfo;
import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.db.DataSourceType;
import com.mogujie.trade.db.ReadWriteSplitting;

/**
 * 单库读写
 */
@DataSourceRouting(dataSource = "simpleDataBase", table = "user_info", isReadWriteSplitting = false)
public interface UserInfoMapper {
	@CacheEvicted(key = { "ABC", "ABC" }, params = { "0.name", "0.nickName" })
	public int insert(UserInfo user);

	@CacheEvicted(key = { "x" }, params = { "0" })
	public int delete(int id);

	@Cached(key = "x", params = "0", cacheNull = true, expire = 600)
	@CacheEvicted(key = "x", params = "0")
	@GroupCacheEvicted(key = "x", groupParam = { "0" }, isAsync = false)
	public UserInfo get(int id);

	@Cached(key = "ABC", params = "0", cacheNull = true, expire = 600)
	public UserInfo getByName(String name);

	/**
	 * <p>
	 * 只要方法上设置了@ReadWriteSplitting
	 * </p>
	 * <p>
	 * 数据源，则以方法上的设置为准，如果方法上没有配置@ReadWriteSplitting，则根据类@DataSourceRouting(isReadWriteSplitting=?)为准。
	 * </p>
	 * <p>
	 * 在支持读写分离的情况下，在方法上没有设置使用数据源的情况时，则会根据方法路由规则来确定是Master还是Slave， <code>也可以通过注册全局
	 * master route dataSource method 名称
	 * ReadWriteSplittingContextInitializer.register(...)
	 * </code>
	 * </p>
	 */
	@ReadWriteSplitting(DataSourceType.slave)
	public void test_no_slave_datasouce();

}

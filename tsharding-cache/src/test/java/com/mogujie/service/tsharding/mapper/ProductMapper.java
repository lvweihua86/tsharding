package com.mogujie.service.tsharding.mapper;

import com.hivescm.tsharding.cache.annotation.TshardingCacheEvicted;
import com.hivescm.tsharding.cache.annotation.TshardingCached;
import com.mogujie.service.tsharding.bean.Product;
import com.mogujie.trade.db.DataSourceRouting;

@DataSourceRouting(dataSource = "product", table = "product", isReadWriteSplitting = true)
public interface ProductMapper {
	@TshardingCacheEvicted(params = "0.id")
	public int insert(Product product);

	@TshardingCacheEvicted(params = "0")
	public int delete(int id);

	@TshardingCached(params = "0")
	public Product get(int id);

	@TshardingCacheEvicted(params = "0")
	public Product getByName(String name);
}

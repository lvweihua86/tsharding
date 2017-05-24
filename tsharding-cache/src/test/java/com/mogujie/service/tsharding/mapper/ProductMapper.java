package com.mogujie.service.tsharding.mapper;

import com.mogujie.service.tsharding.bean.Product;
import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.tsharding.cache.TshardingCacheEvicted;
import com.mogujie.tsharding.cache.TshardingCached;

@DataSourceRouting(dataSource = "product", table = "product", isReadWriteSplitting = true)
public interface ProductMapper {
	public int insert(Product product);

	@TshardingCacheEvicted(params = "0")
	public int delete(int id);

	@TshardingCached(params = "0")
	public Product get(int id);

	public Product getByName(String name);
}

package com.mogujie.service.tsharding.mapper;

import com.hivescm.tsharding.cache.annotation.MapperCacheEvicted;
import com.hivescm.tsharding.cache.annotation.MapperCached;
import com.mogujie.service.tsharding.bean.Product;
import com.mogujie.trade.db.DataSourceRouting;

@DataSourceRouting(dataSource = "product", table = "product", isReadWriteSplitting = true)
public interface ProductMapper {
	@MapperCacheEvicted(key = "x", params = "0.id")
	public int insert(Product product);

	@MapperCacheEvicted(key = "x", params = "0")
	public int delete(int id);

	@MapperCached(key = "x", params = "0")
	public Product get(int id);

	@MapperCacheEvicted(key = "x", params = "0")
	public Product getByName(String name);
}

package com.mogujie.service.tsharding.mapper;

import com.hivescm.tsharding.cache.annotation.MapperCacheEvicted;
import com.hivescm.tsharding.cache.annotation.MapperCached;
import com.mogujie.service.tsharding.bean.Product;
import com.mogujie.trade.db.DataSourceRouting;

@DataSourceRouting(dataSource = "product", table = "product", isReadWriteSplitting = true)
public interface ProductMapper {
	@MapperCacheEvicted(params = "0.id")
	public int insert(Product product);

	@MapperCacheEvicted(params = "0")
	public int delete(int id);

	@MapperCached(params = "0")
	public Product get(int id);

	@MapperCacheEvicted(params = "0")
	public Product getByName(String name);
}

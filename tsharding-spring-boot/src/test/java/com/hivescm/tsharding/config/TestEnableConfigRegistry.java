package com.hivescm.tsharding.config;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.hivescm.tsharding.utils.ClassNameHelper;
import com.mogujie.trade.db.DataSourceRouting;

public class TestEnableConfigRegistry {
	@Test
	public void test_toString() {
		Assert.assertEquals("A,B", ClassNameHelper.create().setMapper(new String[] { "A", "B" }).build());
		Set<Class<?>> set = new HashSet<>();
		String value = ClassNameHelper.create().setMapper(new String[] { "A", "B" }).setMapper(set).build();
		Assert.assertEquals("A,B", value);
		{// 添加一个无效的Mapper
			set.add(TestEnableConfigRegistry.class);
			value = ClassNameHelper.create().setMapper(new String[] { "A", "B" }).setMapper(set).build();
			Assert.assertEquals("A,B", value);
		}
		{
			// 添加一个没有分库分表的Mapper
			set.add(MyMapper.class);
			value = ClassNameHelper.create().setMapper(new String[] { "A", "B" }).setMapper(set).build();
			Assert.assertEquals("A,B", value);
		}
		{
			set.add(MyMapper1.class);
			value = ClassNameHelper.build(new String[] { "A", "B" }, set);
			Assert.assertEquals("A,B,com.hivescm.tsharding.config.TestEnableConfigRegistry$MyMapper1", value);
		}

		{// 测试自动删除重复Mapper
			value = ClassNameHelper.build(
					new String[] { "A", "B", "A", "B", "com.hivescm.tsharding.config.TestEnableConfigRegistry$MyMapper1" }, set);
			Assert.assertEquals("A,B,com.hivescm.tsharding.config.TestEnableConfigRegistry$MyMapper1", value);
		}
	}

	@DataSourceRouting(dataSource = "X", isReadWriteSplitting = false, table = "X", tables = 1)
	public static class MyMapper {

	}

	@DataSourceRouting(dataSource = "X", isReadWriteSplitting = false, table = "X", tables = 2)
	public static class MyMapper1 {

	}
}

package com.hivescm.tsharding.ext;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.hivescm.tsharding.ext.TshardingMapperConfig.Environment;
import com.hivescm.tsharding.ext.TshardingMapperConfig.Mapper;
import com.mogujie.trade.db.DataSourceRouting;

public class TshardingMapperConfigTest {
	//@Test
	//public void test_get_resource_path() throws DocumentException, IOException {
	//	DefaultResourceLoader classPathResource = new DefaultResourceLoader();
	//	Resource resource = classPathResource.getResource("classpath:tsharding_mapper_config.xml");
	//	String path = "/Users/kevin/git/tsharding/tsharding-spring-boot/target/test-classes/tsharding_mapper_config.xml";
	//	Assert.assertEquals(path, resource.getFile().getAbsolutePath());
	//}

	@Test
	public void test_parse_xmlAsPojo() throws DocumentException, IOException {
		DefaultResourceLoader classPathResource = new DefaultResourceLoader();
		Resource resource = classPathResource.getResource("classpath:tsharding_mapper_config.xml");
		TshardingMapperConfig config = TshardingMapperConfig.parse(resource.getInputStream());
		Environment environment = config.getEnvironment("dev");
		Assert.assertTrue(environment.exists(this.getClass()));
		Assert.assertNotNull(environment.getMapper(this.getClass()));
		Assert.assertTrue(environment.exists(AMapper.class));
		Assert.assertNotNull(environment.getMapper(AMapper.class));
		Assert.assertTrue(environment.exists(BMapper.class));
		Assert.assertNotNull(environment.getMapper(BMapper.class));
		{
			environment = config.getEnvironment("t");
			Assert.assertFalse(environment.exists(this.getClass()));
			Assert.assertFalse(environment.exists(AMapper.class));
			Assert.assertFalse(environment.exists(BMapper.class));
		}
	}

	@Test
	public void test_annotation() throws DocumentException, IOException, NoSuchFieldException, IllegalAccessException {
		DataSourceRouting routing = AMapper.class.getAnnotation(DataSourceRouting.class);
		Assert.assertEquals("a", routing.dataSource());
		DefaultResourceLoader classPathResource = new DefaultResourceLoader();
		Resource resource = classPathResource.getResource("classpath:tsharding_mapper_config.xml");
		TshardingMapperConfig config = TshardingMapperConfig.parse(resource.getInputStream());
		Environment environment = config.getEnvironment("dev");
		Assert.assertTrue(environment.exists(AMapper.class));
		Mapper mapper = environment.getMapper(AMapper.class);
		mapper.modifyAnnotation(routing);
		Assert.assertEquals("db_01", routing.dataSource());
		Assert.assertEquals("tab_0001", routing.table());
		Assert.assertEquals(666, routing.tables());
		Assert.assertEquals(true, routing.isReadWriteSplitting());
	}

	@Test(expected = FileNotFoundException.class)
	public void test_err() throws IOException {
		DefaultResourceLoader classPathResource = new DefaultResourceLoader();
		Resource resource = classPathResource.getResource("classpath:xxxx.xml");
		System.out.println(resource.getFile().getAbsolutePath());
	}

}

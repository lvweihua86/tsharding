package com.hivescm.tsharding.scan;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.classes.scan.ClassFilter;
import com.classes.scan.ScanClass;
import com.mogujie.trade.db.DataSourceRouting;

public class ScanMapperUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScanMapperUtils.class);
	private static MapperInfo mapperInfo = null;

	public static MapperInfo scanMapper() {
		if (mapperInfo != null) {
			return mapperInfo;
		}
		synchronized (ScanMapperUtils.class) {
			if (mapperInfo != null) {
				return mapperInfo;
			}
			Set<Class<?>> set = ScanClass.scanPackage("com.hivescm", new ClassFilter() {
				@Override
				public boolean accept(Class<?> clazz) {
					return clazz.getAnnotation(DataSourceRouting.class) != null;
				}
			});
			Set<String> mapperPackage = new HashSet<>();
			Set<Class<?>> enhancedMapper = new HashSet<>();
			for (Class<?> clazz : set) {
				mapperPackage.add(clazz.getPackage().getName());
				DataSourceRouting routing = clazz.getAnnotation(DataSourceRouting.class);
				if (routing.tables() > 1 || routing.databases() > 1) {
					enhancedMapper.add(clazz);
				}
			}
			mapperInfo = new MapperInfo(mapperPackage, enhancedMapper);
			LOGGER.warn("Tsharding.EnhancedMapper:{}", mapperInfo.buildEnhancedMapper());
			LOGGER.warn("Tsharding.Mappers:{}", mapperInfo.buildMappers());
		}
		return mapperInfo;
	}
}

package com.hivescm.tsharding.ext;

import com.hivescm.common.conf.SystemManager;
import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.utils.TShardingLog;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tsharding配置文件处理器
 *
 * @author SHOUSHEN LUAN
 * @author XIEXINDONG
 */
public final class TshardingMapperConfig {
    List<Environment> environments = new ArrayList<>();

    public static TshardingMapperConfig parse(InputStream inputStream) throws DocumentException {
        TshardingMapperConfig config = new TshardingMapperConfig();
        SAXReader saxReader = new SAXReader();
        Document root = saxReader.read(inputStream);
        if ("environments".equalsIgnoreCase(root.getRootElement().getName())) {
            Element nodeElement = root.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> elements = nodeElement.elements();
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                Environment environment = Environment.parser(element);
                config.environments.add(environment);
            }
            return config;
        }
        throw new IllegalArgumentException("无效的配置文件");
    }

    /**
     * Tsharding 环境配置
     *
     * @author SHOUSHEN LUAN
     */
    public static class Environment {
        String name;

        public Environment(String name) {
            this.name = name;
        }

        private List<Mapper> mappers = new ArrayList<>();

        public static Environment parser(Element element) {
            if ("environment".equalsIgnoreCase(element.getName())) {
                Environment environment = new Environment(element.attributeValue("name"));
                Element mappers = element.element("Mappers");
                environment.loadMappers(mappers);
                return environment;
            }
            return null;
        }

        public void loadMappers(Element element) {
            if (element == null) {
                return;
            }
            @SuppressWarnings("unchecked")
            List<Element> mapperList = element.elements();
            for (int i = 0; i < mapperList.size(); i++) {
                Element mapper = mapperList.get(i);
                if (mapper.getName().equalsIgnoreCase("Mapper")) {
                    mappers.add(Mapper.parser(mapper));
                }
            }
        }

        public Mapper getMapper(Class<?> clazz) {
            // 优先查找精确匹配的
            for (Mapper mapper : mappers) {
                if (mapper.matches(clazz) && !StringUtils.hasText(mapper.mapperClassRegex)) {
                    return mapper;
                }
            }
            // 如果没有，则按照正则查找匹配的配置
            for (Mapper mapper : mappers) {
                if (mapper.matches(clazz)) {
                    return mapper;
                }
            }
            return null;
        }

        public boolean exists(Class<?> clazz) {
            // 无论是精确还是正则，只要有匹配即可
            for (Mapper mapper : mappers) {
                if (mapper.matches(clazz)) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * TSharding Mapper配置
     *
     * @author SHOUSHEN LUAN
     */
    public static class Mapper {
        private static final String REG_MODE_PREFIX = "+";
        private static final String OPTIONAL_MODE_PREFIX = "?";
        Class<?> mapperClazz;
        String mapperClassRegex;
        String dataSource;
        Integer databases;
        String table;
        Integer tables;
        Boolean isReadWriteSplitting;
        boolean isRegMode = false;
        boolean isOptionalMode = false;

        void parserMapperClass(Element element) {
            String value = element.attributeValue("class");
            if (value == null || value.trim().length() == 0) {
                throw new IllegalArgumentException("<Mapper class='必须的'/>");
            }
            if (value.startsWith(REG_MODE_PREFIX)) {
                isRegMode = true;
                mapperClassRegex = value.substring(1);
            } else {
                if (value.startsWith(OPTIONAL_MODE_PREFIX)) {
                    isOptionalMode = true;
                    value = value.substring(1);
                }
                try {
                    this.mapperClazz = Class.forName(value.trim());
                } catch (Throwable ex) {
                    if (!isOptionalMode) {
                        ex.printStackTrace();
                        throw new IllegalArgumentException("<Mapper class='" + value + "'... 无效的Class");
                    }
                }
                if (!isOptionalMode && !this.mapperClazz.isInterface()) {
                    throw new IllegalArgumentException("<Mapper class='" + value + "'... 不合法的Class");
                }
            }
        }

        public boolean matches(Class<?> clazz) {
            if (clazz == null) {
                return false;
            }
            if (this.mapperClazz != null) {
                return this.mapperClazz.getName().equals(clazz.getName());
            } else if (StringUtils.hasText(this.mapperClassRegex)) {
                return clazz.getName().matches(this.mapperClassRegex);
            } else {
                return false;
            }
        }

        public static Mapper parser(Element element) {
            Mapper mapper = new Mapper();
            mapper.parserMapperClass(element);
            String val = null;
            mapper.dataSource = element.attributeValue("dataSource");
            try {
                val = element.attributeValue("databases");
                if (val != null) {
                    mapper.databases = Integer.parseInt(val);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("databases=`" + val + "`");
            }
            mapper.table = element.attributeValue("table");
            try {
                val = element.attributeValue("tables");
                if (val != null) {
                    mapper.tables = Integer.parseInt(element.attributeValue("tables"));
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("tables=`" + val + "`");
            }
            try {
                val = element.attributeValue("isReadWriteSplitting");
                if (val != null) {
                    mapper.isReadWriteSplitting = Boolean.parseBoolean(val);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("isReadWriteSplitting=`" + val + "`");
            }

            return mapper;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void modifyAnnotation(DataSourceRouting routing) throws NoSuchFieldException, IllegalAccessException {
            InvocationHandler h = Proxy.getInvocationHandler(routing);
            Field hField = h.getClass().getDeclaredField("memberValues");
            hField.setAccessible(true);
            Map memberValues = (Map) hField.get(h);
            if (StringUtils.hasText(this.dataSource)) {
                memberValues.put("dataSource", this.dataSource);
            }
            if (!StringUtils.hasText(mapperClassRegex) && StringUtils.hasText(table)) {
                memberValues.put("table", table);
            }
            if (tables != null) {
                memberValues.put("tables", tables);
            }
            if (databases != null) {
                memberValues.put("databases", databases);
            }
            if (this.isReadWriteSplitting != null) {
                memberValues.put("isReadWriteSplitting", isReadWriteSplitting);
            }
        }
    }

    /**
     * 获取环境配置
     *
     * @param env
     * @return
     */
    public Environment getEnvironment(String env) {
        for (Environment environment : environments) {
            if (environment.name.equalsIgnoreCase(env)) {
                return environment;
            }
        }
        // null环境
        return new Environment(env);
    }

    public void modifyAnnotation(Set<Class<?>> mappers) throws NoSuchFieldException, IllegalAccessException {
        String env = SystemManager.getEnv();
        Environment environment = getEnvironment(env);
        for (Class<?> clazz : mappers) {
            if (environment.exists(clazz)) {
                Mapper mapper = environment.getMapper(clazz);
                DataSourceRouting routing = clazz.getAnnotation(DataSourceRouting.class);
                mapper.modifyAnnotation(routing);
                TShardingLog.getLogger().warn("当前系统环境：" + env + "|修改Mapper:" + clazz);
            }
        }
    }
}

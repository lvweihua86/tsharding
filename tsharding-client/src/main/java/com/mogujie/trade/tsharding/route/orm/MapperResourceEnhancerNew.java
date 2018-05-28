package com.mogujie.trade.tsharding.route.orm;

import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.hander.SQLEnhancerHander;
import com.mogujie.trade.utils.TShardingLog;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mappper sql增强
 * <p>
 * 扩展MapperResourceEnhancer不支持嵌套多标签的增强SQL处理
 * </p>
 * <p>
 * xiexindong:修复为支持嵌套多标签SQL导致语句修改错误问题(因对象是引用类型，直接修改SQL语句会导致后续分表无法执行替换)
 * 1、修复嵌套多标签SQL语句处理问题；
 * 2、持续增强SQL语句处理，支持全部语法下关联语句子查询等；
 * 3、提升SQL语句处理性能
 * </p>
 *
 * @author qigong on 5/1/15
 * @author xiexindong on 2018-05-23
 */
@SuppressWarnings("unchecked")
public class MapperResourceEnhancerNew extends MapperEnhancer {

    public MapperResourceEnhancerNew(Class<?> mapperClass) {
        super(mapperClass);
    }

    private SQLEnhancerHander getShardingTableHander(MappedStatement ms) {
        Class<?> clazzClass = getMapperClass(ms.getId());
        DataSourceRouting sharding = clazzClass.getAnnotation(DataSourceRouting.class);
        Class<?> enhancerHanderClass = sharding.sqlEnhancerHander();
        try {
            SQLEnhancerHander hander = (SQLEnhancerHander) enhancerHanderClass.getConstructor(Class.class)
                    .newInstance(getMapperClass(ms.getId()));
            return hander;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 增强SQL处理
     *
     * @param ms
     * @param configuration
     * @param shardingPara
     * @return
     */
    public SqlSource enhancedShardingSQL(MappedStatement ms, Configuration configuration, Long shardingPara) {
        SQLEnhancerHander hander = getShardingTableHander(ms);
        TShardingLog.getLogger().debug(ms.getId());
        try {
            if (ms.getSqlSource() instanceof DynamicSqlSource) {
                DynamicSqlSource dynamicSqlSource = processDynamicSqlSource(hander, (DynamicSqlSource) ms.getSqlSource(), configuration, shardingPara);
                return dynamicSqlSource;
            } else if (ms.getSqlSource() instanceof RawSqlSource) {
                RawSqlSource sqlSource = (RawSqlSource) ms.getSqlSource();
                MetaObject sqlSourceObject = forObject(sqlSource);
                StaticSqlSource staticSqlSource = (StaticSqlSource) sqlSourceObject.getValue("sqlSource");
                MetaObject staticSqlSourceMetaObject = forObject(staticSqlSource);
                String sql = (String) staticSqlSourceMetaObject.getValue("sql");
                if (hander.hasReplace(sql)) {
                    sql = hander.format(sql, shardingPara);
                    SqlSource result = new RawSqlSource(configuration, sql, null);
                    MetaObject resultObject = forObject(result);
                    // 为sqlSource对象设置mappering参数
                    StaticSqlSource newStaticSqlSource = (StaticSqlSource) resultObject.getValue("sqlSource");
                    List<ParameterMapping> parameterMappings = (List<ParameterMapping>) staticSqlSourceMetaObject.getValue("parameterMappings");
                    MetaObject newStaticSqlSourceMetaObject = forObject(newStaticSqlSource);
                    newStaticSqlSourceMetaObject.setValue("parameterMappings", parameterMappings);
                    return result;
                } else {
                    return sqlSource;
                }
            } else {
                throw new RuntimeException("wrong sqlSource type!" + ms.getResource());
            }
        } catch (Exception e) {
            TShardingLog.error("reflect error!, ms resources:" + ms.getResource(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 处理动态SQL源
     *
     * @param hander
     * @param sqlSource
     * @param configuration
     * @param shardingPara
     * @return
     */
    private DynamicSqlSource processDynamicSqlSource(SQLEnhancerHander hander, DynamicSqlSource sqlSource, Configuration configuration, Long shardingPara) {
        DynamicSqlSource dynamicSqlSource;
        try {
            MetaObject metaObject = forObject(sqlSource);
            MixedSqlNode rootSqlNode = (MixedSqlNode) metaObject.getValue("rootSqlNode");
            if (rootSqlNode == null) {
                return sqlSource;
            }
            MixedSqlNode shardingRootSqlNode = processMixedSqlNode(hander, rootSqlNode, shardingPara);
            dynamicSqlSource = new DynamicSqlSource(configuration, shardingRootSqlNode);
        } catch (Exception e) {
            e.printStackTrace();
            dynamicSqlSource = sqlSource;
        }
        return dynamicSqlSource;
    }

    /**
     * 处理混合SQL节点
     *
     * @param hander
     * @param sqlNode
     * @param shardingPar
     * @return
     */
    private MixedSqlNode processMixedSqlNode(SQLEnhancerHander hander, SqlNode sqlNode, Long shardingPar) {
        MixedSqlNode mixedSqlNode = (MixedSqlNode) sqlNode;
        MetaObject metaobject = forObject(mixedSqlNode);
        Object value = metaobject.getValue("contents");
        if (value == null) {
            return mixedSqlNode;
        }
        List<SqlNode> contents = (List<SqlNode>) value;
        List<SqlNode> shardingSqlNodeList = processSqlNodeList(hander, contents, shardingPar);
        return new MixedSqlNode(shardingSqlNodeList);
    }

    /**
     * 处理if类型SQL节点
     *
     * @param hander
     * @param sqlNode
     * @param shardingPar
     * @return
     */
    private IfSqlNode processIfSqlNode(SQLEnhancerHander hander, SqlNode sqlNode, Long shardingPar) {
        IfSqlNode ifSqlNode = (IfSqlNode) sqlNode;
        MetaObject metaobject = forObject(ifSqlNode);
        Object value = metaobject.getValue("contents");
        if (value == null) {
            return ifSqlNode;
        }
        SqlNode contents = (SqlNode) value;
        SqlNode shardingNode = processNormalSqlNode(hander, contents, shardingPar);
        IfSqlNode shardingIfSqlNode = new IfSqlNode(shardingNode, (String) metaobject.getValue("test"));
        return shardingIfSqlNode;
    }

    /**
     * 处理静态SQL节点
     *
     * @param hander
     * @param sqlNode
     * @param shardingPar
     * @return
     */
    private StaticTextSqlNode processStaticTextSqlNode(SQLEnhancerHander hander, SqlNode sqlNode, Long shardingPar) {
        StaticTextSqlNode staticTextSqlNode = (StaticTextSqlNode) sqlNode;
        MetaObject metaobject = forObject(staticTextSqlNode);
        String text = (String) metaobject.getValue("text");
        if (text == null) {
            return staticTextSqlNode;
        }
        if (hander.hasReplace(text)) {
            text = hander.format(text, shardingPar);
        }
        StaticTextSqlNode shardingNode = new StaticTextSqlNode(text);
        return shardingNode;
    }

    /**
     * 处理文本SQL节点
     *
     * @param hander
     * @param sqlNode
     * @param shardingPar
     * @return
     */
    private TextSqlNode processTextSqlNode(SQLEnhancerHander hander, SqlNode sqlNode, Long shardingPar) {
        TextSqlNode textSqlNode = (TextSqlNode) sqlNode;
        MetaObject metaobject = forObject(textSqlNode);
        String text = (String) metaobject.getValue("text");
        if (text == null) {
            return textSqlNode;
        }
        if (hander.hasReplace(text)) {
            text = hander.format(text, shardingPar);
        }
        TextSqlNode shardingNode = new TextSqlNode(text);
        return shardingNode;
    }

    /**
     * 处理foreach类型SQL节点
     *
     * @param hander
     * @param sqlNode
     * @param shardingPar
     * @return
     */
    private ForEachSqlNode processForEachSqlNode(SQLEnhancerHander hander, SqlNode sqlNode, Long shardingPar) {
        ForEachSqlNode forEachSqlNode = (ForEachSqlNode) sqlNode;
        MetaObject metaobject = forObject(forEachSqlNode);
        Object value = metaobject.getValue("contents");
        if (value == null) {
            return forEachSqlNode;
        }
        SqlNode contents = (SqlNode) value;
        SqlNode shardingSqlNode = processNormalSqlNode(hander, contents, shardingPar);
        String collectionExpression = (String) metaobject.getValue("collectionExpression");
        String open = (String) metaobject.getValue("open");
        String close = (String) metaobject.getValue("close");
        String separator = (String) metaobject.getValue("separator");
        String item = (String) metaobject.getValue("item");
        String index = (String) metaobject.getValue("index");
        Configuration configuration = (Configuration) metaobject.getValue("configuration");
        ForEachSqlNode shardingNode = new ForEachSqlNode(configuration, shardingSqlNode, collectionExpression, index, item, open, close, separator);
        return shardingNode;
    }

    /**
     * 处理choose类型SQL节点
     *
     * @param hander
     * @param sqlNode
     * @param shardingPar
     * @return
     */
    private ChooseSqlNode processChooseSqlNode(SQLEnhancerHander hander, SqlNode sqlNode, Long shardingPar) {
        ChooseSqlNode chooseSqlNode = (ChooseSqlNode) sqlNode;
        MetaObject metaobject = forObject(chooseSqlNode);
        Object value = metaobject.getValue("ifSqlNodes");
        Object defaultValue = metaobject.getValue("defaultSqlNode");
        if (value == null && defaultValue == null) {
            return chooseSqlNode;
        }
        List<SqlNode> ifSqlNodes = (List<SqlNode>) value;
        List<SqlNode> shardingSqlNodeList = processSqlNodeList(hander, ifSqlNodes, shardingPar);
        SqlNode defaultSqlNode = (SqlNode) defaultValue;
        SqlNode shardingSqlNode = processNormalSqlNode(hander, defaultSqlNode, shardingPar);
        return new ChooseSqlNode(shardingSqlNodeList, shardingSqlNode);
    }

    /**
     * 处理trim、where、set类型SQL节点
     *
     * @param hander
     * @param sqlNode
     * @param shardingPar
     * @return
     */
    private TrimSqlNode processTrimSqlNode(SQLEnhancerHander hander, SqlNode sqlNode, Long shardingPar) {
        TrimSqlNode trimSqlNode = (TrimSqlNode) sqlNode;
        MetaObject metaobject = forObject(trimSqlNode);
        Object value = metaobject.getValue("contents");
        if (value == null) {
            return trimSqlNode;
        }
        SqlNode contents = (SqlNode) value;
        SqlNode shardingNode = processNormalSqlNode(hander, contents, shardingPar);
        Configuration configuration = (Configuration) metaobject.getValue("configuration");
        TrimSqlNode shardingTrimSqlNode;
        if (sqlNode instanceof SetSqlNode) {
            shardingTrimSqlNode = new SetSqlNode(configuration, shardingNode);
        } else if (sqlNode instanceof WhereSqlNode) {
            shardingTrimSqlNode = new WhereSqlNode(configuration, shardingNode);
        } else {
            String prefix = (String) metaobject.getValue("prefix");
            String suffix = (String) metaobject.getValue("suffix");
            List<String> prefixesToOverrideList = (List<String>) metaobject.getValue("prefixesToOverride");
            String prefixesToOverride = join(" |", prefixesToOverrideList);
            List<String> suffixesToOverrideList = (List<String>) metaobject.getValue("suffixesToOverride");
            String suffixesToOverride = join(" |", suffixesToOverrideList);
            shardingTrimSqlNode = new TrimSqlNode(configuration, shardingNode, prefix, prefixesToOverride, suffix, suffixesToOverride);
        }
        return shardingTrimSqlNode;
    }

    /**
     * 处理SQL节点集合
     *
     * @param hander
     * @param contents
     * @param shardingPar
     * @return
     */
    private List<SqlNode> processSqlNodeList(SQLEnhancerHander hander, List<SqlNode> contents, Long shardingPar) {
        if (contents == null) {
            return contents;
        }
        List<SqlNode> shardingSqlNodeList = new ArrayList<>(contents.size());
        for (SqlNode node : contents) {
            SqlNode shardingNode = processNormalSqlNode(hander, node, shardingPar);
            shardingSqlNodeList.add(shardingNode);
        }
        return shardingSqlNodeList;
    }

    /**
     * 根据SQL节点类型做相应的处理
     *
     * @param hander
     * @param node
     * @param shardingPar
     * @return
     */
    private SqlNode processNormalSqlNode(SQLEnhancerHander hander, SqlNode node, Long shardingPar) {
        if (node == null) {
            return node;
        }
        SqlNode shardingNode;
        if (node instanceof MixedSqlNode) {
            shardingNode = processMixedSqlNode(hander, node, shardingPar);
        } else if (node instanceof IfSqlNode) {
            shardingNode = processIfSqlNode(hander, node, shardingPar);
        } else if (node instanceof StaticTextSqlNode) {
            shardingNode = processStaticTextSqlNode(hander, node, shardingPar);
        } else if (node instanceof TextSqlNode) {
            shardingNode = processTextSqlNode(hander, node, shardingPar);
        } else if (node instanceof ForEachSqlNode) {
            shardingNode = processForEachSqlNode(hander, node, shardingPar);
        } else if (node instanceof ChooseSqlNode) {
            shardingNode = processChooseSqlNode(hander, node, shardingPar);
        } else if (node instanceof TrimSqlNode) {
            shardingNode = processTrimSqlNode(hander, node, shardingPar);
        } else {
            shardingNode = node;
        }
        return shardingNode;
    }

    /**
     * String.join 依赖 java8，先自行实现
     *
     * @param split
     * @param values
     * @return
     */
    private String join(String split, List<String> values) {
        String result = null;
        if (values != null && values.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String item : values) {
                stringBuilder.append(item);
                stringBuilder.append(split);
            }
            result = stringBuilder.toString();
            result = result.substring(0, result.length() - split.length());
        }
        return result;
    }
}
package com.mogujie.service.tsharding.test;


import com.mogujie.route.rule.AbstractRouteRule;
import com.mogujie.route.rule.RouteRule;
import com.mogujie.trade.db.DataSourceRouting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 根据sharding参数取余分库，分表数量获取到分库分表后缀，根据设置bit位长度不足补零
 * <p>
 * 路由规则：
 * </p>
 * <ul>
 * <li>路由dataSource: value%DataSourceRouting.database</li>
 * <li>获得DataSource:datasource0000~datasourceN-1</li>
 * <li>路由Table：value%DataSourceRouting.tables</li>
 * <li>获得Table table0000~tableN-1</li>
 * </ul>
 *
 *
 */
public class TestRoteRule extends AbstractRouteRule<Long> implements RouteRule<Long> {
    private static final Logger logger = LoggerFactory.getLogger(TestRoteRule.class);
    /**
     * 分表名称后缀固定长度，长度不足补零
     */
    private final int tableBit = 1;
    /**
     * 分库名称后缀固定长度，长度不足补零
     */
    private final int schemaBit = 1;

    private String calcSchemaNameSuffix(DataSourceRouting routing, Long shardingVal) {
        int index = getDataSourceSuffix(routing, shardingVal);
        return fillBit(index, getSchemaBit());
    }

    @Override
    public String calculateSchemaName(DataSourceRouting routing, Long shardingVal) {
        String schemaSuffix = calcSchemaNameSuffix(routing, shardingVal);
        String schemaName = routing.dataSource() + schemaSuffix;
        logger.info("WmsRouteRule-schemaName=" + schemaName);
        return schemaName;
    }

    /**
     * 根据分片参数值计算分表名
     *
     * @param routing
     * @param shardingVal
     * @return 分表名0xxx
     */
    @Override
    public String calculateTableNameSuffix(DataSourceRouting routing, Long shardingVal) {
        int index = getTableSuffix(routing, shardingVal);
        String tableNameSuffix = fillBit(index, getTableBit());
        logger.info("WmsRouteRule-tableNameSuffix=" + tableNameSuffix);
        return tableNameSuffix;
    }

    /**
     * 根据分片参数值计算分表名
     *
     * @param shardingPara
     * @return 分表名0xxx
     */
    @Override
    public String calculateTableName(DataSourceRouting routing, Long shardingPara) {
        String tableSuffix = calculateTableNameSuffix(routing, shardingPara);
        return routing.table() + tableSuffix;
    }

    public int getTableBit() {
        return tableBit;
    }

    public int getSchemaBit() {
        return schemaBit;
    }

    @Override
    public String fillBit(long shardingTableSuffix) {
        return fillBit(shardingTableSuffix, getTableBit());
    }

    @Override
    public int getDataSourceSuffix(DataSourceRouting routing, Long shardingVal) {
        if (shardingVal < 1) {
            throw new IllegalArgumentException("分库分表参数必须大于等于1");
        }
        if (routing.databases() > 1) {
            long val = shardingVal % routing.databases();
            return (int) val;
        }
        throw new IllegalArgumentException("注解实例的分库数量必须大于1");
    }

    @Override
    public int getTableSuffix(DataSourceRouting routing, Long shardingVal) {
        if (shardingVal < 1) {
            throw new IllegalArgumentException("分库分表参数必须大于等于1");
        }
        if (routing.tables() > 1) {
            if (shardingVal < 100L) {
                return 0;
            } else {
                String shardingValStr = shardingVal.toString();
                shardingValStr = shardingValStr.substring(0, shardingValStr.length() - 2);
                return Integer.valueOf(shardingValStr) % routing.tables();
            }
        } else {
            return 0;
        }
    }

    public String fillDataSourceBit(long shardingDataSourceSuffix) {
        return fillBit(shardingDataSourceSuffix, getSchemaBit());
    }

    private final String fillBit(long shardingTableSuffix, int bit) {
        if (shardingTableSuffix < 0) {
            throw new IllegalArgumentException("shardingTableSuffix:`" + shardingTableSuffix + "` 必须大于等于零");
        } else if (bit < 1) {
            throw new IllegalArgumentException("bit:`" + bit + "` 必须大于零");
        }
        char[] chat = String.valueOf(shardingTableSuffix).toCharArray();
        if (chat.length > bit) {// 数值长度必须小于生成的长度
            throw new IllegalArgumentException("shardingTableSuffix:`" + shardingTableSuffix + "`位数超过生成总长度");
        }
        StringBuilder builder = new StringBuilder(bit);
        for (short s = 0; s < bit; s++) {
            if (s < chat.length) {
                builder.append(chat[s]);
            } else {
                builder.insert(0, '0');
            }
        }
        return builder.toString();
    }
}
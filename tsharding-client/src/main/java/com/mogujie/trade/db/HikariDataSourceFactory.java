package com.mogujie.trade.db;

import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 使用HikariCP连接池
 * <p>
 * https://github.com/brettwooldridge/HikariCP
 * </p>
 * 
 * @CreateTime 2016年10月26日 下午6:01:24
 * @author SHOUSHEN LUAN
 */
public class HikariDataSourceFactory implements DataSourceFactory<HikariDataSource> {

    @Override
    public HikariDataSource getDataSource(DataSourceConfig config) throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        if (config.getDriver() != null) {
            hikariConfig.setDriverClassName(config.getDriver());
        }
        HikariDataSource ds = new HikariDataSource(hikariConfig);
        ds.setMaximumPoolSize(config.getMaxPoolSize());
        ds.setMinimumIdle(config.getMinPoolSize());
        ds.setConnectionTimeout(3000);
        ds.setIdleTimeout(30000);
        return ds;
    }

}

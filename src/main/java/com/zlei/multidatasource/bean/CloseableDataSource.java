package com.zlei.multidatasource.bean;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author： zhanlei
 * Description：
 * CreateTime: 23:30 2017/12/25
 * Modified by:
 */
public class CloseableDataSource {

    private static final Logger logger = LoggerFactory.getLogger(CloseableDataSource.class);

    private DataSource dataSource;

    private Long lastAccquireTime;

    public CloseableDataSource(DataSource dataSource) {
        Assert.isInstanceOf(DruidDataSource.class, dataSource, "only support DruidDataSource");
        this.dataSource = dataSource;
        lastAccquireTime = System.currentTimeMillis();
    }

    public void close() {
        if (dataSource instanceof DruidDataSource) {
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;
            try {
                if (!druidDataSource.isInited()) {
                    return;
                }
                druidDataSource.restart();
            } catch (SQLException e) {
                logger.error("dataSource close exception", e);
                throw new RuntimeException(e.getCause());
            }
        }
        throw new RuntimeException("dataSource type not supported, only DruidDataSource can apply");
    }

    public DataSource getDataSource() {
        this.lastAccquireTime = System.currentTimeMillis();
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long getLastAccquireTime() {
        return lastAccquireTime;
    }
}

package com.zlei.multidatasource.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author：zhanlei
 * Description：动态数据源配置
 * CreateTime: 23:16 2017/12/25
 * Modified by:
 */
@ConfigurationProperties(prefix = "druid.datasource.dynamic")
public class DynamicDSProperties {

    /** 该值取url或username 针对mysql和oracle，默认为url */
    private String uniqueDataSourceKey = "url";

    /** 默认关闭该功能，因为druid数据源有关闭连接池的设置，如果只有几个数据源可忽略该配置 */
    private boolean enableCloseTask = false;

    /** 启用enableCloseTask时使用，数据源闲置的最大时间，超过时间不使用就会关闭已有的连接池，默认10分钟*/
    private long datasourceCloseInterval = 1000 * 60 * 10;

    /** 启用enableCloseTask时使用，多少时间执行一次数据源关闭检测 默认10分钟*/
    private long releaseIntervel = 1000 * 60 * 10;

    public String getUniqueDataSourceKey() {
        return uniqueDataSourceKey;
    }

    public void setUniqueDataSourceKey(String uniqueDataSourceKey) {
        this.uniqueDataSourceKey = uniqueDataSourceKey;
    }

    public boolean isEnableCloseTask() {
        return enableCloseTask;
    }

    public void setEnableCloseTask(boolean enableCloseTask) {
        this.enableCloseTask = enableCloseTask;
    }

    public long getDatasourceCloseInterval() {
        return datasourceCloseInterval;
    }

    public void setDatasourceCloseInterval(long datasourceCloseInterval) {
        this.datasourceCloseInterval = datasourceCloseInterval;
    }

    public long getReleaseIntervel() {
        return releaseIntervel;
    }

    public void setReleaseIntervel(long releaseIntervel) {
        this.releaseIntervel = releaseIntervel;
    }
}

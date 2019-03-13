package com.zlei.multidatasource.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zlei.multidatasource.bean.CacheKeyEnum;
import com.zlei.multidatasource.bean.CloseableDataSource;
import com.zlei.multidatasource.bean.SwitchInfo;
import com.zlei.multidatasource.thread.DataSourceReleaseRunnable;
import com.zlei.multidatasource.thread.SwitchInfoThreadLocal;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author： zhanlei
 * Description：
 * CreateTime: 23:23 2017/12/25
 * Modified by:
 */
public class DynamicDatasource extends AbstractDataSource {

    private final Map<String, CloseableDataSource> finalDataSourceMap = new ConcurrentHashMap<>();

    private CloseableDataSource defaultCloseableDataSource;

    private static final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    private static final long defaultReleaseIntervel = 1000 * 60 * 10;

    private DataSource defaultDataSource;

    private boolean userDefaultIfNotFound;

    private CacheKeyEnum keyEnum;

    /** 为新创建的datasource跟随默认配置，用于存储默认配置的DruidDataSource */
    private DruidDataSource defaultProperties = new DruidDataSource();

    private DynamicDSProperties dynamicDSProperties;

    public DynamicDatasource(DataSource defaultDataSource, DynamicDSProperties dynamicDSProperties) {
        Assert.notNull(defaultDataSource, "defaultDataSource can not be null");
        Assert.isInstanceOf(DruidDataSource.class, defaultDataSource, "can not construct default datasource, only druid datasource supported");
        this.defaultDataSource = defaultDataSource;
        this.dynamicDSProperties = dynamicDSProperties != null ? dynamicDSProperties : new DynamicDSProperties();
        this.keyEnum = CacheKeyEnum.getEnumByKey(dynamicDSProperties.getUniqueDataSourceKey());
    }

    /** 设置为public是为了在外部手动释放连接的操作 */
    public Map<String, CloseableDataSource> getFinalDataSourceMap() {
        return finalDataSourceMap;
    }

    public DataSource getDefaultDataSource() {
        return defaultDataSource;
    }

    public void setDefaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public boolean isUserDefaultIfNotFound() {
        return userDefaultIfNotFound;
    }

    public void setUserDefaultIfNotFound(boolean userDefaultIfNotFound) {
        this.userDefaultIfNotFound = userDefaultIfNotFound;
    }

    public CloseableDataSource getDefaultCloseableDataSource() {
        return defaultCloseableDataSource;
    }

    public void setDefaultCloseableDataSource(CloseableDataSource defaultCloseableDataSource) {
        this.defaultCloseableDataSource = defaultCloseableDataSource;
    }

    public DynamicDSProperties getDynamicDSProperties() {
        return dynamicDSProperties;
    }

    public void setDynamicDSProperties(DynamicDSProperties dynamicDSProperties) {
        this.dynamicDSProperties = dynamicDSProperties;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
    }

    /**
     * Retrieve the current target DataSource. Determines the
     */
    protected DataSource determineTargetDataSource() {
        Assert.notEmpty(this.finalDataSourceMap, "finalDataSourceMap can not be empty");
        SwitchInfo switchInfo = determineCurrentLookupKey();
        CloseableDataSource cdToUse = null;
        if (switchInfo == null) {
            return defaultCloseableDataSource.getDataSource();
        }

        CloseableDataSource cachedDataSource = getCachedDataSource(switchInfo);
        if (cachedDataSource == null) {
            if (userDefaultIfNotFound) {
                return defaultCloseableDataSource.getDataSource();
            }
            DruidDataSource dsToUse = new DruidDataSource();
            BeanUtils.copyProperties(defaultProperties, dsToUse);
            // 设置自定义属性
            if (!StringUtils.isEmpty(switchInfo.getUrl())) {
                dsToUse.setUrl(switchInfo.getUrl());
            }
            if (!StringUtils.isEmpty(switchInfo.getUserName())) {
                dsToUse.setUsername(switchInfo.getUserName());
            }
            if (!StringUtils.isEmpty(switchInfo.getPassword())) {
                dsToUse.setPassword(switchInfo.getPassword());
            }

            try {
                dsToUse.init();
                cdToUse = new CloseableDataSource(dsToUse);
                cacheDataSource(cdToUse);
                cachedDataSource = cdToUse;
            } catch (Exception e) {
                logger.error("datasource init error, message:");
                throw new RuntimeException("datasource init error ",e);
            }
        }

        return cachedDataSource.getDataSource();
    }

    /**
     * Determine the current lookup key. This will typically be
     * implemented to check a thread-bound transaction context.
     */
    protected SwitchInfo determineCurrentLookupKey() {
        return SwitchInfoThreadLocal.getSwichInfo();
    }

    public void afterPropertiesSet() {
        Assert.notNull(defaultDataSource, "defaultDatasource can not be null");
        Assert.isInstanceOf(DruidDataSource.class, defaultDataSource, "only support druidDataSource");

        BeanUtils.copyProperties(defaultDataSource, defaultProperties);
        try {
            ((DruidDataSource)defaultDataSource).init();
        } catch (Exception e) {
            logger.error("init default druidDatasource error", e);
            throw new RuntimeException("init default druidDatasource error", e);
        }

        defaultCloseableDataSource = new CloseableDataSource(defaultDataSource);
        cacheDataSource(defaultCloseableDataSource);
        if (dynamicDSProperties.isEnableCloseTask()) {
            initReleaseTask();
        }
    }

    private void initReleaseTask() {
        DataSourceReleaseRunnable releaseRunnable = new DataSourceReleaseRunnable(this, dynamicDSProperties.getDatasourceCloseInterval());
        taskScheduler.initialize();
        try {
            taskScheduler.scheduleAtFixedRate(releaseRunnable,
                    dynamicDSProperties.getReleaseIntervel() > 0 ? dynamicDSProperties.getReleaseIntervel() : defaultReleaseIntervel);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    private CloseableDataSource getCachedDataSource(SwitchInfo switchInfo) {
        Assert.notNull(switchInfo, "switchInfo can not be null");
        switch (this.keyEnum) {
            case URL: {
                return finalDataSourceMap.get(Optional.ofNullable(switchInfo.getUrl()).orElseThrow(()-> new RuntimeException("url can not be empty")));
            }
            case USER_NAME:{
                return finalDataSourceMap.get(Optional.ofNullable(switchInfo.getUserName()).orElseThrow(()-> new RuntimeException("username url can not be empty")));
            }
            default:{
                throw new RuntimeException("unique cache key is not supported, please make sure your uniqueCache key is url or username");
            }
        }
    }

    private void cacheDataSource(CloseableDataSource closeableDataSource) {
        Assert.notNull(closeableDataSource, "can not store null datasource");
        switch (this.keyEnum) {
            case URL: {
                finalDataSourceMap.put(((DruidDataSource) closeableDataSource.getDataSource()).getUrl(), closeableDataSource);
                break;
            }
            case USER_NAME:{
                finalDataSourceMap.put(((DruidDataSource) closeableDataSource.getDataSource()).getUsername(), closeableDataSource);
                break;
            }
            default:{
                throw new RuntimeException("unique cache key is not supported, please make sure you uniqueCache key is url or username");
            }
        }

    }
}

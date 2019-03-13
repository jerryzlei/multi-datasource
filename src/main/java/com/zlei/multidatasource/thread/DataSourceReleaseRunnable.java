package com.zlei.multidatasource.thread;

import com.zlei.multidatasource.bean.CloseableDataSource;
import com.zlei.multidatasource.config.DynamicDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author： zhanlei
 * Description：
 * CreateTime: 23:45 2017/12/26
 * Modified by:
 */
public class DataSourceReleaseRunnable implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(DataSourceReleaseRunnable.class);

    private  DynamicDatasource dynamicDatasource;

    private long releaseIntervel = 1000 * 60 * 10;

    public DataSourceReleaseRunnable(DynamicDatasource dynamicDatasource, Long releaseIntervel) {
        Assert.notNull(dynamicDatasource, "can not constructor null value");
        this.dynamicDatasource = dynamicDatasource;
        if (releaseIntervel != null) {
            this.releaseIntervel = releaseIntervel;
        }
    }

    private boolean shouldRelease(CloseableDataSource closeableDataSource) {
        return  (System.currentTimeMillis() - closeableDataSource.getLastAccquireTime() > releaseIntervel);
    }

    public long getReleaseIntervel() {
        return releaseIntervel;
    }

    public void setReleaseIntervel(long releaseIntervel) {
        this.releaseIntervel = releaseIntervel;
    }

    @Override
    public void run() {
        logger.info("---------------执行数据源清理任务，时间：{}---------------", System.currentTimeMillis());
        Map<String, CloseableDataSource> finalMap = dynamicDatasource.getFinalDataSourceMap();
        for (CloseableDataSource closeableDataSource : finalMap.values()) {
            if (shouldRelease(closeableDataSource)) {
                try {
                    closeableDataSource.close();
                } catch (Exception e) {
                    throw new RuntimeException("close datasource fail", e.getCause());
                }
            }
        }
    }
}

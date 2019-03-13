package com.zlei.multidatasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.zlei.multidatasource.config.DefaultDsSwitchAop;
import com.zlei.multidatasource.config.DsSwitchAop;
import com.zlei.multidatasource.config.DynamicDSProperties;
import com.zlei.multidatasource.config.DynamicDatasource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @author： zhanlei
 * Description：
 * CreateTime: 23:13 2017/12/25
 * Modified by:
 */
@Configuration
@ConditionalOnClass(DruidDataSource.class)
@EnableConfigurationProperties({DynamicDSProperties.class})
public class DynamicDataSourceAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConfigurationProperties(prefix = "druid.datasource")
    public DruidDataSource defaultDataSource() {
        return new DruidDataSource();
    }

    @Bean
    @Primary
    public DataSource DynamicDataSource(@Qualifier("defaultDataSource") DataSource defaultDatasource, DynamicDSProperties dsProperties) {
        DynamicDatasource dynamicDatasource = new DynamicDatasource(defaultDatasource, dsProperties);
        dynamicDatasource.afterPropertiesSet();
        return dynamicDatasource;
    }

    @Bean
    @ConditionalOnMissingBean(DsSwitchAop.class)
    public DefaultDsSwitchAop DefaultDsSwitchAop() {
        return new DefaultDsSwitchAop();
    }

    /**
     * only support transactionManager of datasource
     */
    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

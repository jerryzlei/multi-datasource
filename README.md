# 应用背景
由于spring-jdbc支持的动态数据源配置不支持运行时动态加入数据源，因此笔者在其源代码的基础上稍作修改，
使其能够支持运行时动态切换数据源，从而进行切换数据库操作的功能。


# 使用方式     
使用方式如下所示，只需要在最终调用数据库的方法内传入包含数据库关键信息的SwitchInfo即可达到动态切换，
当不使用@SwitchDataSource注解时使用默认的数据源配置
````
@SwitchDataSource
public List<User> testSwitch(SwitchInfo switchInfo) {
    List<User> users = userMapper.selectAll();
    return users;
}
````
# Maven配置
除了下属配置外，还需额外引用部分该工具正常运行所以来的包，比如AspectJ、Spring-jdbc、Druid...
````
<dependency>
     <groupId>com.github.jerryzlei</groupId>
     <artifactId>multi-datasource</artifactId>
      <version>1.0.2.RELEASE</version>
</dependency>
````
# 属性配置举例
* 在springboot的application.properties文件配置举例如下，新增的数据源除了关键属性不一样其它都与该配置相同：

```
# druid数据源配置
druid.datasource.url=jdbc:mysql://127.0.0.1:3306/testmail?allowMultiQueries=true&useSSL=true&serverTimezone=Asia/Shanghai
druid.datasource.username=root
druid.datasource.password=eewasder
druid.datasource.driverClassName=com.mysql.jdbc.Driver
druid.datasource.initialSize=1
druid.datasource.minIdle=5
....
# 动态数据源配置
## 设置数据源新增时的key，不同数据源保持唯一，只有url和username2种，不配置默认为url
druid.datasource.dynamic.uniqueDataSourceKey=url 
## 默认关闭该功能，因为druid数据源有关闭连接池的设置
druid.datasource.dynamic.enableCloseTask=false
## 启用enableCloseTask时使用，数据源闲置的最大时间，超过时间不使用就会关闭已有的连接池，单位毫秒，默认10分钟
druid.datasource.dynamic.datasourceCloseInterval=600000
## 启用enableCloseTask时使用，多少时间执行一次数据源关闭检测 默认10分钟
druid.datasource.dynamic.releaseIntervel=600000
```

* 内置配置自带datasource的事物管理器，可省略配置，使用mybatis框架配置时不需配置DataSource，直接去spring内部取即可

```
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {
    /**
     * main start.
     * @param args args
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactoryBean(DataSource dynamicDataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dynamicDataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mapper/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }
}
```
* 可自定义自己的切换注解，比如给注解定义参数，根据参数来进行数据源切换，并继承DsSwitchAop同时注入到spring容器中完成自定义aop配置，就可完成自定义扩展


# 本项目在以下代码托管网站
* GitHub：https://github.com/jerryzlei/multi-datasource.git

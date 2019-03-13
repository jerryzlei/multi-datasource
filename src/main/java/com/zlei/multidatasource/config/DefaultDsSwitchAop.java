package com.zlei.multidatasource.config;

import com.zlei.multidatasource.bean.SwitchInfo;
import com.zlei.multidatasource.thread.SwitchInfoThreadLocal;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * @author： zhanlei
 * Description：
 * CreateTime: 22:58 2017/12/26
 * Modified by:
 */
@Aspect
public class DefaultDsSwitchAop implements DsSwitchAop, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDsSwitchAop.class);

    @Pointcut("@annotation(com.zlei.multidatasource.bean.SwitchDataSource)")
    protected void switchDataSource() {
    }

    @Override
    @Before("switchDataSource()")
    public void switchBeforeMethod(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof SwitchInfo) {
                SwitchInfoThreadLocal.setSwitchInfo((SwitchInfo) arg);
                return;
            }
        }
        logger.warn("------------添加切换注解上的方法缺少SwitchInfo参数------------");
    }

    @Override
    @After("switchDataSource()")
    public void switchAfterMethod() {
        SwitchInfoThreadLocal.removeSwitchInfo();
    }

    /** 要使数据源切换生效必须让其先于spring事物aop执行 */
    @Override
    public int getOrder() {
        return -1;
    }
}

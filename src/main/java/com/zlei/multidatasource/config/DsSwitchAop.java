package com.zlei.multidatasource.config;

import org.aspectj.lang.JoinPoint;

/**
 * @author： zhanlei
 * Description：
 * CreateTime: 17:47 2017/12/30
 * Modified by:
 */
public interface DsSwitchAop {

    void switchBeforeMethod(JoinPoint joinPoint);

    void switchAfterMethod();
}

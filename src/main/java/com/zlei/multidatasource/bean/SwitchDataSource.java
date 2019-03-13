package com.zlei.multidatasource.bean;

import java.lang.annotation.*;

/**
 * @author： zhanlei
 * Description：
 * CreateTime: 22:55 2017/12/26
 * Modified by:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SwitchDataSource {
}

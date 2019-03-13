package com.zlei.multidatasource.thread;

import com.zlei.multidatasource.bean.SwitchInfo;

/**
 * @author： zhanlei
 * Description：
 * CreateTime: 23:43 2017/12/25
 * Modified by:
 */
public class SwitchInfoThreadLocal {

    private static final ThreadLocal<SwitchInfo> threadLocal = new ThreadLocal<>();

    public static SwitchInfo getSwichInfo() {
        return threadLocal.get();
    }

    public static void setSwitchInfo(SwitchInfo switchInfo) {
        threadLocal.set(switchInfo);
    }

    public static void removeSwitchInfo() {
        threadLocal.remove();
    }
}

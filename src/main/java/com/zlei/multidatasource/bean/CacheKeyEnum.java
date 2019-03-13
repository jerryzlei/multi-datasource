package com.zlei.multidatasource.bean;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author：zhanlei
 * Description: datasource的存储方式
 * CreateTime: 13:33 2017/12/30
 * Modified by:
 */
public enum CacheKeyEnum {

    URL("url", "数据源的url"),
    USER_NAME("username","数据源的username");

    private String key;

    private String desc;

    CacheKeyEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public static CacheKeyEnum getEnumByKey(String key) {
        Assert.isTrue(!StringUtils.isEmpty(key),"CacheKeyEnum key can not be empty");;
        for (CacheKeyEnum ckEnum: CacheKeyEnum.values()) {
            if (key.equals(ckEnum.getKey())) {
                return ckEnum;
            }
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

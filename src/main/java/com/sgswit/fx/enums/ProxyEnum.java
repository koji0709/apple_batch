package com.sgswit.fx.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DeZh
 * @title: ProxyEnum
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2516:53
 */
public enum ProxyEnum {
    NONE("0","不使用代理",5),
    API("1","API或导入代理",600),
    TUNNEL("2","使用隧道代理",600),
    DEFAULT("3","使用内置代理",40),
    ;
    private String  key;
    private String  value;
    private Integer maxThreadCount;

    ProxyEnum(String key, String value, Integer maxThreadCount) {
        this.key = key;
        this.value = value;
        this.maxThreadCount = maxThreadCount;
    }

    public static List<Map<String,Object>> getProxyModeList() {
        List<Map<String,Object>> list=new ArrayList<>();
        for (ProxyEnum proxyEnum : values()) {
            list.add(new HashMap<>(){{
                put("key",proxyEnum.key);
                put("value",proxyEnum.value);
                put("maxThreadCount",proxyEnum.maxThreadCount);
            }});
        }
        return list;
    }
}

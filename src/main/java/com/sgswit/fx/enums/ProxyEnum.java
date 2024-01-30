package com.sgswit.fx.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理参数枚举类
 * @author DeZh
 * @title: ProxyEnum
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2516:53
 */
public class ProxyEnum{
    /**
    　* 代理模式
      * @param
    　* @return
    　* @throws
    　* @author DeZh
    　* @date 2024/1/25 21:36
    */
    public enum Mode{
            NONE("0","不使用代理",5),
            API("1","API或导入代理",600),
            TUNNEL("2","使用隧道代理",600),
            DEFAULT("3","使用内置代理",40),
                    ;
            private String  key;
            private String  value;
            private Integer maxThreadCount;

            Mode(String key, String value, Integer maxThreadCount) {
                this.key = key;
                this.value = value;
                this.maxThreadCount = maxThreadCount;
            }

            public static List<Map<String,Object>> getProxyModeList() {
                List<Map<String,Object>> list=new ArrayList<>();
                for (Mode proxyEnum : values()) {
                    list.add(new HashMap<>(){{
                        put("key",proxyEnum.key);
                        put("value",proxyEnum.value);
                        put("maxThreadCount",proxyEnum.maxThreadCount);
                    }});
                }
                return list;
            }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Integer getMaxThreadCount() {
            return maxThreadCount;
        }

        public void setMaxThreadCount(Integer maxThreadCount) {
            this.maxThreadCount = maxThreadCount;
        }
    }
    /**
     * 代理类型
     * @param
    　* @return
    　* @throws
    　* @author DeZh
    　* @date 2024/1/25 21:36
     */
    public enum Type{
            Http("1","Http/Https"),
            Socks4("2","Socks4"),
            Socks5("3","Socks5"),
            Socks4a("4","Socks4a"),
            Socks5h("5","Socks5h"),
                    ;
            private String  key;
            private String  value;

            Type(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public static List<Map<String,String>> getProxyTypeList() {
                List<Map<String,String>> list=new ArrayList<>();
                for (Type proxyEnum : values()) {
                    list.add(new HashMap<>(){{
                        put("key",proxyEnum.key);
                        put("value",proxyEnum.value);
                    }});
                }
                return list;
            }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }




}

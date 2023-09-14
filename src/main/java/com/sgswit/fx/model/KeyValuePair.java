package com.sgswit.fx.model;

/**
 * @author DeZh
 * @title: KeyValuePair
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1220:09
 */
public class KeyValuePair {
    private String key;
    private String value;
    private String path;
    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }
    public KeyValuePair(String key, String value, String path) {
        this.key = key;
        this.value = value;
        this.path = path;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getPath() {
        return path;
    }
}

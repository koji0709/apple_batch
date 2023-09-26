package com.sgswit.fx.ext;

/**
 * @author DeZh
 * @title: ItemConsumer
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/2122:13
 */
public interface ItemConsumer<T> {
    void setProperties(T t,String value);
}

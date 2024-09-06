package com.sgswit.fx;

import cn.hutool.db.Entity;

import java.util.Set;

public class ThreadLocalProxyInfo {

    // 使用 ThreadLocal 实例来存储 Entity 对象
    private static final ThreadLocal<Entity> threadLocalEntity = new ThreadLocal<>();

    public static Entity get() {
        return threadLocalEntity.get();
    }

    public static void set(Entity entity) {
        threadLocalEntity.set(entity);
    }

    public static void remove() {
        threadLocalEntity.remove();
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadLocalProxyInfo.set(new Entity().set("key1","value1"));

        Thread.sleep(2000L);
        System.err.println(ThreadLocalProxyInfo.get());
    }

}

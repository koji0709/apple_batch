package com.sgswit.fx.utils;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.GlobalThreadPool;
import cn.hutool.core.thread.ThreadFactoryBuilder;

import java.nio.channels.Channel;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author DeZh
 * @title: ThreadTest
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/1812:22
 */
public class ThreadPoolUtil {
    private static ExecutorService pool = ExecutorBuilder.create()
            //初始20个线程
            .setCorePoolSize(5)
            //最大40个线程
            .setMaxPoolSize(5)
            //有界等待队列，最大等待数是60
//            .setWorkQueue(new LinkedBlockingQueue <>(5))
            //设置线程前缀
            .setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("IM-Pool-").build())
            .build();

    public static void main(String[] args) {
        try {
            for (int i=0;i<100;i++) {
                int finalI = i;
                pool.submit(() -> System.out.println(finalI));
            }
            System.out.println(111);
            pool.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


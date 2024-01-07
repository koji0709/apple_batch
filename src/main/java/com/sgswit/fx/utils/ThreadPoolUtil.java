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
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author DeZh
 * @title: ThreadTest
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/1812:22
 */
public class ThreadPoolUtil {

    public static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);


    public static ExecutorService getFixedThreadPool() {
        if(fixedThreadPool.isShutdown()){
            fixedThreadPool = Executors.newFixedThreadPool(1);
        }
        return fixedThreadPool;

    }

}


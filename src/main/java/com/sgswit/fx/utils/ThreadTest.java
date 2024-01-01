package com.sgswit.fx.utils;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.KeyValuePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * @author DeZh
 * @title: ThreadTest
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/1812:22
 */
public class ThreadTest {
  public static void main(String[] args) throws InterruptedException {
    // 初始化线程数量锁
    CountDownLatch countDownLatch = ThreadUtil.newCountDownLatch(5);
    for (int i = 0; i < 5; i++) {
        String name = i + ": 线程执行了";
        ThreadUtil.execute(() -> {
            // todo 逻辑操作
            System.out.println(name);
            //当前线程执行完后,调用线程计数器-1
            countDownLatch.countDown();
        });
    }
    //唤醒主线程
    countDownLatch.await();
    System.out.println("全部执行完成 !");
    }
}


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
    /**
     * 初始化线程池，同时执行2个线程
     */
    private static ExecutorService executor = ThreadUtil.newExecutor(1);

    public  static void main(String[] args) {
//        mainPoolExecutor();
    }

    //===其他测试=================
    public static void mainPoolExecutor() throws InterruptedException {
        //线程池设置名称
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNamePrefix("cyf-").build();
        //线程数,线程池中的最大线程数,空闲存活时间,参数keepAliveTime的时间单位,阻塞队列
        ExecutorService pool = new ThreadPoolExecutor(10, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        List<Integer> sum = Arrays.asList(1,2,3,4,5,6);
        List<Callable<Integer>> tasks = new ArrayList<>();
        long t = System.currentTimeMillis();
        for (int i : sum) {
            tasks.add(new TaskTest(i));
        }
        List<Future<Integer>> all = pool.invokeAll(tasks);
        all.stream()
                .forEach(
                        k -> {
                            try {
                                System.out.println(k.get());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        });
        pool.shutdown();
        System.out.println("任务结束时间:"+(System.currentTimeMillis()-t));
    }

    private static class TaskTest implements Callable<Integer> {

        private Integer sum = 0;
        public TaskTest(int sum){
            this.sum = sum;
        }
        @Override
        public Integer call() throws Exception {
            System.out.println("任务开始执行："+Thread.currentThread().getName());
            Thread.sleep(1000);
            System.out.println("任务进入执行："+Thread.currentThread().getName());
            return sum+10;
        }
    }
}

package com.sgswit.fx.utils;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author DeZh
 * @title: SingleInstanceApp
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/11/1520:06
 */
public class SingleInstanceApp {
    public static void main(String[] args) {
        try {
            // 获取文件锁
            FileLock lock = FileChannel.open(
                    Paths.get(System.getProperty("user.dir"), "single_instance.lock"),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)
                    .tryLock();

            // 如果获取锁失败，说明程序已经在运行
            if (lock == null) {
                System.out.println("Program is already running.");
                return;
            }

            // 添加守护线程，程序退出时释放锁
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    lock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            // 程序正常运行逻辑
            System.out.println("Program is running.");

            // 模拟程序运行
            Thread.sleep(5000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

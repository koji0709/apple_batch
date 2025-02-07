package com.sgswit.fx.utils.proxy;

import cn.hutool.db.Entity;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.utils.PropertiesUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IPManager {
    private final Queue<Entity> ipPool = new ConcurrentLinkedQueue<>();
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    // 单例实例
    private static volatile IPManager instance;

    // 定期清理过期数据的线程池
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // 私有构造方法，防止外部实例化
    private IPManager() {
        //reloadIPs(); // 初始化 IP 池
        startExpirationChecker(); // 启动过期检查任务
    }

    /**
     * 获取单例实例
     *
     * @return IPManager 实例
     */
    public static IPManager getInstance() {
        if (instance == null) {
            synchronized (IPManager.class) {
                if (instance == null) {
                    instance = new IPManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取一个 IP。如果池为空，会阻塞直到新的 IP 加入。
     *
     * @return 一个可用的 IP 对象
     */
    public Entity getIP() {
        Entity ip;

        int retryCount = 0;
        int maxRetries = 3;
        while ((ip = ipPool.poll()) == null) {
            if (retryCount >= maxRetries) {
                return null;
            }
            reloadIPs();
            retryCount++;
        }
        return ip;
    }

    /**
     * 从外部接口重新加载 IP。
     */
    private void reloadIPs() {
        lock.lock();
        try {
            // 防止多线程同时触发 reload
            if (!ipPool.isEmpty()) {
                return;
            }
            String proxyUrl = PropertiesUtil.getConfig("proxyUrl");
            HashMap<String, List<String>> headers = new HashMap<>(10);
            HttpResponse result = HttpRequest.get(proxyUrl)
                    .timeout(10000)
                    .header(headers)
                    .execute();
            JSON jsonObject = JSONUtil.parse(result.body());
            String code = jsonObject.getByPath("code", String.class);
            if ("0".equals(code)) {
                List<Map<String, Object>> dataList = jsonObject.getByPath("data", List.class);
                for (Map<String, Object> map : dataList) {
                    Entity entity = new Entity();
                    entity.setTableName("proxy_ip_info");
                    entity.set("id", map.get("id"));
                    entity.set("ip", map.get("ip"));
                    entity.set("port", map.get("port"));
                    entity.set("username", map.get("username"));
                    entity.set("pwd", map.get("pwd"));
                    entity.set("last_update_time", 0);
                    entity.set("input_time", System.currentTimeMillis());
                    entity.set("expiration_time", map.get("expiration_time"));
                    entity.set("protocol_type", map.get("protocol_type"));
                    ipPool.offer(entity);
                }
            }
            notEmpty.signalAll(); // 通知等待的线程
        } finally {
            lock.unlock();
        }
    }

    /**
     * 启动一个定时任务，清理过期的 IP。
     */
    private void startExpirationChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            ipPool.removeIf(entity -> {
                long expirationTime = entity.getLong("expiration_time");
                return expirationTime <= currentTime - 3000;
            });
        }, 0, 1, TimeUnit.SECONDS); // 每秒检查一次
    }

}

package com.sgswit.fx.utils.proxy;

import cn.hutool.db.Entity;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.utils.PropertiesUtil;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IPManager2 {
    private final List<Entity> ipPool = new CopyOnWriteArrayList<>();
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    private static volatile IPManager2 instance;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private IPManager2() {
        startExpirationChecker(); // 启动过期检查任务
    }

    public static IPManager2 getInstance() {
        if (instance == null) {
            synchronized (IPManager2.class) {
                if (instance == null) {
                    instance = new IPManager2();
                }
            }
        }
        return instance;
    }

    /**
     * 获取一个使用次数最少的随机 IP。如果池为空，会阻塞直到新的 IP 加入。
     *
     * @return 一个可用的 IP 对象
     */
    public Entity getIP() {
        Entity ip = null;
        int retryCount = 0;
        int maxRetries = 3;

        while (ip == null) {
            lock.lock();
            try {
                if (ipPool.isEmpty()) {
                    if (retryCount >= maxRetries) {
                        return null;
                    }
                    reloadIPs();
                    retryCount++;
                } else {
                    // 筛选出使用次数最少的 IP
                    int minUseCount = ipPool.stream()
                            .mapToInt(entity -> entity.getInt("useCount"))
                            .min()
                            .orElse(0);

                    List<Entity> leastUsedIPs = new ArrayList<>();
                    for (Entity entity : ipPool) {
                        if (entity.getInt("useCount") == minUseCount) {
                            leastUsedIPs.add(entity);
                        }
                    }

                    // 从使用次数最少的 IP 中随机选择一个
                    int randomIndex = ThreadLocalRandom.current().nextInt(leastUsedIPs.size());
                    ip = leastUsedIPs.get(randomIndex);

                    // 更新使用次数
                    ip.set("useCount", ip.getInt("useCount") + 1);
                }
            } finally {
                lock.unlock();
            }
        }
        return ip;
    }

    private void reloadIPs() {
        lock.lock();
        try {
            if (!ipPool.isEmpty()) {
                return;
            }
            System.out.println("重新加载 IP 池...");
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
                    entity.set("useCount", 0); // 初始化使用次数
                    ipPool.add(entity);
                }
            }
            notEmpty.signalAll(); // 通知等待的线程
        } finally {
            lock.unlock();
        }
    }

    private void startExpirationChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            ipPool.removeIf(entity -> {
                long expirationTime = entity.getLong("expiration_time");
                return expirationTime <= currentTime - 10000;
            });
        }, 0, 1, TimeUnit.SECONDS);
    }
}

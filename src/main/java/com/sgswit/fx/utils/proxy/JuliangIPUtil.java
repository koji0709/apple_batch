package com.sgswit.fx.utils.proxy;

import cn.hutool.db.Entity;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.utils.PropertiesUtil;

import java.net.Authenticator;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JuliangIPUtil {

    // 用户名密码认证(动态代理/独享代理)
    final static String ProxyUser = "17608177103";
    final static String ProxyPass = "33KNiSOX";

    private final Queue<Entity> ipPool = new ConcurrentLinkedQueue<>();
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    // 单例实例
    private static volatile JuliangIPUtil instance;

    // 定期清理过期数据的线程池
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // 私有构造方法，防止外部实例化
    private JuliangIPUtil() {
    }

    /**
     * 获取单例实例
     *
     * @return IPManager 实例
     */
    public static JuliangIPUtil getInstance() {
        if (instance == null) {
            synchronized (IPManager.class) {
                if (instance == null) {
                    instance = new JuliangIPUtil();
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

            String url1 = "http://v2.api.juliangip.com/dynamic/getips?auto_white=1&filter=1&num=20&pt=1&result_type=json&trade_no=1806944562528701&sign=dbf4ee798584a31aae559a5db8c1f4b4";
            String resp = HttpUtil.get(url1);
            JSON response = JSONUtil.parse(resp);
            JSONArray proxyArr = response.getByPath("data.proxy_list", JSONArray.class);

            System.out.println("重新加载 IP 池...");
            proxyArr.forEach(proxy -> {
                String p = (String) proxy;
                    Entity entity = new Entity();
                    entity.setTableName("proxy_ip_info");
                    entity.set("ip", p.split(":")[0]);
                    entity.set("port", p.split(":")[1]);
                    entity.set("username", "17608177103");
                    entity.set("pwd", "33KNiSOX");
                    entity.set("last_update_time", 0);
                    entity.set("input_time", System.currentTimeMillis());
                    entity.set("expiration_time", System.currentTimeMillis() + 60 * 1000);
                    entity.set("protocol_type", "1");
                    ipPool.offer(entity);
            });
            notEmpty.signalAll(); // 通知等待的线程
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {

        String url1 = "http://v2.api.juliangip.com/dynamic/getips?auto_white=1&filter=1&num=20&pt=1&result_type=json&trade_no=1806944562528701&sign=dbf4ee798584a31aae559a5db8c1f4b4";
        String resp = HttpUtil.get(url1);
        JSON response = JSONUtil.parse(resp);
        JSONArray proxyArr = response.getByPath("data.proxy_list", JSONArray.class);

        proxyArr.forEach(proxy -> {
            String p = (String) proxy;
            System.err.println(p);
        });
    }

}

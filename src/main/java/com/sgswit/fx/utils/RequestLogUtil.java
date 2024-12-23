package com.sgswit.fx.utils;

import cn.hutool.core.net.NetUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.utils.proxy.ProxyInfo;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestLogUtil {

    private static final ConcurrentLinkedQueue<Map<String, Object>> cacheQueue = new ConcurrentLinkedQueue<>();
    private static final int BATCH_SIZE = 100;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void record(String url, String method, ProxyInfo proxy, long timeInMillis, int status,Date reqeustTime, String message) {
        // 创建请求日志的参数
        String localIp = NetUtil.getLocalhostStr();

        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("method", method);
        params.put("took", timeInMillis / 1000);
        params.put("message", message);
        params.put("status", status);
        params.put("localIp", localIp);
        params.put("requestTime",reqeustTime);
        if (proxy != null) {
            params.put("host", proxy.getHost());
            params.put("port", proxy.getPort());
        }

        // 添加到线程安全的队列中
        cacheQueue.add(params);

        // 触发异步保存操作
        if (cacheQueue.size() >= BATCH_SIZE) {
            triggerBatchSave();
        }
    }

    private static void triggerBatchSave() {
        // 提交异步任务
        executor.submit(() -> {
            List<Map<String, Object>> batchList = new ArrayList<>();
            while (batchList.size() < BATCH_SIZE && !cacheQueue.isEmpty()) {
                Map<String, Object> log = cacheQueue.poll();
                if (log != null) {
                    batchList.add(log);
                }
            }

            if (!batchList.isEmpty()) {
                // 执行批量保存
                HttpResponse response = HttpUtils.post("/requestLog/batchSave", JSONUtil.toJsonStr(batchList));
                boolean success = HttpUtils.verifyRsp(response);
                if (!success) {
                    LoggerManger.info("Failed to save request logs.");
                }
            }
        });
    }

}


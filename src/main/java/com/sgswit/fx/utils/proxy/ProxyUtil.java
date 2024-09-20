package com.sgswit.fx.utils.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.db.Entity;
import cn.hutool.http.*;
import com.sgswit.fx.ThreadLocalProxyInfo;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.common.UnavailableException;
import com.sgswit.fx.enums.ProxyEnum;
import com.sgswit.fx.utils.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author DeZh
 * @title: ProxyUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2511:34
 */
public class ProxyUtil {

    private static final ConcurrentHashMap<String, Instant> uriTimestamps = new ConcurrentHashMap<>();

    private static final long MIN_INTERVAL_MS = 200;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static Map<String, Integer> map503Error = new HashMap<>(16);
    private static Map<String, Integer> mapIoError = new HashMap<>(16);

    private static final ReentrantLock lock = new ReentrantLock();

    public static HttpResponse execute(HttpRequest request) {
        return execute(request,true);
    }

    public static HttpResponse execute(HttpRequest request,Boolean readTimeoutTry) {
        HttpRequest.closeCookie();
        if (Thread.currentThread().isInterrupted()) {
            throw new ServiceException("请求失败：停止任务");
        }

        String requestId = MD5.create().digestHex(request.toString());
        boolean isRedeem = request.getUrl().contains("/WebObjects/MZFinance.woa/wa/redeemCodeSrv");
        boolean isITunes = request.getUrl().contains("/WebObjects/MZFinance.woa");

        int sleepTime = isITunes ? 50 : 100;
        int try503Num = isRedeem ? 10 : 25;
        int tryIoNum = 4;

        HttpResponse httpResponse;
        try {
            if (isRedeem){
                lock.lock();
            }
            httpResponse = createRequest(request).execute();
            LoggerManger.info(String.format("[%s] %s, Response.status = %d",request.getMethod(),request.getUrl().split("\\?")[0], httpResponse.getStatus()));
            if (httpResponse.getStatus() == 503) {
                // 重试
                handleRetry(requestId, sleepTime, try503Num, map503Error);
                return execute(request);
            }
        } catch (IORuntimeException | HttpException e) {
            // 网络异常处理
            handleIoException(requestId, sleepTime, tryIoNum, e, isRedeem,readTimeoutTry);
            return execute(request);
        } finally {
            if (isRedeem){
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                }
                lock.unlock();
            }
            map503Error.remove(requestId);
            mapIoError.remove(requestId);
        }
        return httpResponse;
    }

    private static void handleRetry(String requestId, int sleepTime, int tryNum, Map<String, Integer> errorMap) {
        ThreadUtil.sleep(sleepTime);

        int failCount = errorMap.getOrDefault(requestId, 0) + 1;
        errorMap.put(requestId, failCount);

        if (failCount > tryNum) {
            throw new UnavailableException();
        }
    }

    private static void handleIoException(String requestId, int sleepTime, int tryIoNum, Exception e, boolean isRedeem,boolean readTimeoutTry) {
        LoggerManger.info("网络异常, message = " + e.getMessage());
        // 需要重试的异常
        if (isRetryableError(e.getMessage())) {
            // 重试
            handleRetry(requestId, sleepTime, tryIoNum, mapIoError);
        } else {
            if (!StringUtils.containsIgnoreCase(e.getMessage(), "Read timed out")){
                throw new ServiceException("网络连接异常，请稍后重试");
            }
            if (readTimeoutTry){
                handleRetry(requestId,sleepTime,tryIoNum,mapIoError);
            }else{
                throw new ServiceException(isRedeem ? "兑换响应超时, 请检查兑换状态" : "服务响应超时, 请稍后重试");
            }
        }
    }

    private static boolean isRetryableError(String message) {
        return StringUtils.containsIgnoreCase(message, "connect") ||
                StringUtils.containsIgnoreCase(message, "connection") ||
                StringUtils.containsIgnoreCase(message, "503 Service Unavailable") ||
                StringUtils.containsIgnoreCase(message, "407 Proxy Authentication Required") ||
                StringUtils.containsIgnoreCase(message, "authentication failed") ||
                StringUtils.containsIgnoreCase(message, "Remote host terminated the handshake") ||
                StringUtils.containsIgnoreCase(message, "SOCKS: Network unreachable") ||
                StringUtils.containsIgnoreCase(message, "460 Proxy Authentication Invalid")

                ;
    }

    private static HttpRequest createRequest(HttpRequest request) {
        String proxyMode = PropertiesUtil.getOtherConfig("proxyMode");
        int sendTimeOut = PropertiesUtil.getOtherInt("sendTimeOut");
        sendTimeOut = sendTimeOut == 0 ? 5 * 1000 : sendTimeOut * 1000;
        int readTimeOut = 5 * 1000;

        // 未选择代理, 或者配置错误, 则不使用代理
        if (StringUtils.isEmpty(proxyMode)
                || !Arrays.asList(ProxyEnum.Mode.API.getKey(), ProxyEnum.Mode.TUNNEL.getKey(), ProxyEnum.Mode.DEFAULT.getKey()).contains(proxyMode)) {
            return proxyRequest(request, sendTimeOut, readTimeOut);
        }

        // API或导入代理
        if (ProxyEnum.Mode.API.getKey().equals(proxyMode)) {
            //判断是否为空
            String proxyApiUrl = PropertiesUtil.getOtherConfig("proxyApiUrl");
            String proxyApiUser = PropertiesUtil.getOtherConfig("proxyApiUser");
            String proxyApiPass = PropertiesUtil.getOtherConfig("proxyApiPass");
            boolean proxyApiNeedPass = PropertiesUtil.getOtherBool("proxyApiNeedPass", false);
            if (!StringUtils.isEmpty(proxyApiUrl)) {
                return apiProxyRequest(request, proxyApiUrl, proxyApiUser, proxyApiPass, proxyApiNeedPass, sendTimeOut, readTimeOut);
            }
            return proxyRequest(request, sendTimeOut, readTimeOut);
        }

        // 使用隧道代理
        if (ProxyEnum.Mode.TUNNEL.getKey().equals(proxyMode)) {
            String address = PropertiesUtil.getOtherConfig("proxyTunnelAddress");
            String proxyHost = address.split(":")[0];
            int proxyPort = Integer.valueOf(address.split(":")[1]);
            String authUser = PropertiesUtil.getOtherConfig("proxyTunnelUser");
            String authPassword = PropertiesUtil.getOtherConfig("proxyTunnelPass");
            return proxyRequest(request, proxyHost, proxyPort, authUser, authPassword, sendTimeOut, readTimeOut, getProxyType(""));
        }

        // 使用内置代理, 兜底代码
        List<Map<String, Object>> proxyConfigList = DataUtil.getProxyConfig();
        if (CollUtil.isEmpty(proxyConfigList)) {
            return proxyRequest(request, sendTimeOut, readTimeOut);
        }

        //根据权重配比，随机获取一种
        int[] weights = new int[proxyConfigList.size()];
        int i = 0;
        for (Map<String, Object> map : proxyConfigList) {
            weights[i] = MapUtil.getInt(map, "weight");
            i++;
        }
        int index = StrUtils.getWeightedRandomIndex(weights);
        Map<String, Object> proxyConfigMap = proxyConfigList.get(index);
        String proxyType = MapUtil.getStr(proxyConfigMap, "proxyType");

        //LoggerManger.info("权重计算代理IP信息。 proxyConfigList= " + proxyConfigList + ", proxyConfigMap = " + proxyConfigMap);
        if (StrUtil.isNotEmpty(proxyType)){
            // 私密代理
            if ( "1".equals(proxyType)) {
                Entity entity = ApiProxyUtil.getRandomIp();
                if (!CollUtil.isEmpty(entity)) {
                    String proxyHost = entity.getStr("ip");
                    int proxyPort = entity.getInt("port");
                    String username = entity.getStr("username");
                    String pwd = entity.getStr("pwd");
                    String protocol_type = entity.getStr("protocol_type");
                    username = AesUtil.decrypt(username);
                    pwd = AesUtil.decrypt(pwd);
                    return proxyRequest(request, proxyHost, proxyPort, username, pwd, sendTimeOut, readTimeOut, getProxyType(protocol_type));
                } else { // 如果获取不到代理ip, 则先使用隧道代理
                   proxyType = "2";
                   proxyConfigMap = proxyConfigList.stream()
                            .filter(map -> "2".equals(map.get("proxyType")))
                            .findFirst()
                            .get();
                   LoggerManger.info("获取代理IP失败, 切换至隧道代理。 proxyConfigList= " + proxyConfigList + ", proxyConfigMap = " + proxyConfigMap);
                }
            }

            // 隧道代理
            if ("2".equals(proxyType)) {
                String protocolType = MapUtil.getStr(proxyConfigMap, "protocolType");
                String authUser = MapUtil.getStr(proxyConfigMap, "account");
                String authPassword = MapUtil.getStr(proxyConfigMap, "pwd");
                authUser = AesUtil.decrypt(authUser);
                authPassword = AesUtil.decrypt(authPassword);
                proxyConfigMap.put("account", authUser);
                proxyConfigMap.put("pwd", authPassword);
                return proxyRequest(request, proxyConfigMap, sendTimeOut, readTimeOut, getProxyType(protocolType));
            }
        }

        return proxyRequest(request,sendTimeOut,readTimeOut);
    }

    private static HttpRequest createRequestTest(HttpRequest request) {
        int sendTimeOut = 10 * 1000;
        int readTimeOut = 10 * 1000;

        Entity entity = getProxyInfo();
        String protocol_type = entity.getStr("protocol_type");

        String proxyHost = entity.getStr("ip");
        int proxyPort = entity.getInt("port");
        String username = entity.getStr("username");
        String pwd = entity.getStr("pwd");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return proxyRequest(request, proxyHost, proxyPort, username, pwd, sendTimeOut, readTimeOut, getProxyType(protocol_type));
    }

    public static Entity getProxyInfo() {
        Entity entity = ThreadLocalProxyInfo.get();
        if (CollUtil.isEmpty(entity)) {
            entity = new Entity();
            entity.set("protocol_type", "1");
            entity.set("ip", "k786.kdltps.com");
            entity.set("port", "15818");
            entity.set("username", "t11194129891958");
            entity.set("pwd", "7gz517yn" + ":" + RandomUtil.randomNumbers(5));
            ThreadLocalProxyInfo.set(entity);
        }
        return entity;
    }

    private static HttpRequest apiProxyRequest(HttpRequest request, String proxyApiUrl, String proxyApiUser, String proxyApiPass, Boolean proxyApiNeedPass, int sendTimeOut, int readTimeout) {
        if (Validator.isUrl(proxyApiUrl)) {
            HttpResponse response = HttpUtil.createRequest(Method.GET, proxyApiUrl).execute();
            if (response.getStatus() == 200) {
                String body = response.body();
                List<String> lines = Arrays.asList(body.split("\r\n"));
                if (null != lines && lines.size() > 0) {
                    if (!proxyApiNeedPass) {
                        return getIpByTxt(lines, request, "", "", sendTimeOut, readTimeout);
                    } else {
                        return getIpByTxt(lines, request, proxyApiUser, proxyApiPass, sendTimeOut, readTimeout);
                    }
                }
            }
            return request.timeout(sendTimeOut);
        } else {
            //本地模式
            List<String> lines = FileUtil.readLines(new File(proxyApiUrl), Charset.defaultCharset());
            if (null != lines && lines.size() > 0) {
                if (!proxyApiNeedPass) {
                    return getIpByTxt(lines, request, "", "", sendTimeOut, readTimeout);
                } else {
                    return getIpByTxt(lines, request, proxyApiUser, proxyApiPass, sendTimeOut, readTimeout);
                }
            }
            return request.timeout(sendTimeOut);
        }
    }

    private static HttpRequest getIpByTxt(List<String> lines, HttpRequest request, String authUser, String authPassword, int sendTimeOut, int readTimeout) {
        int index = ThreadLocalRandom.current().nextInt(lines.size()) % lines.size();
        String[] arr = lines.get(index).split(":");
        String host = arr[0];
        int port = Integer.valueOf(arr[1]);
        return proxyRequest(request, host, port, authUser, authPassword, sendTimeOut, readTimeout, getProxyType(""));
    }

    private static HttpRequest proxyRequest(HttpRequest request, String proxyHost, Integer proxyPort, String authUser, String authPassword, int sendTimeOut, int readTimeout, Proxy.Type proxyType) {
        LoggerManger.info(String.format("[%s] %s, proxy = %s:%d, unique = %s",request.getMethod(), request.getUrl().split("\\?")[0], proxyHost, proxyPort, authPassword));
        // 设置请求验证信息
        Authenticator.setDefault(new ProxyAuthenticator(authUser, authPassword));
        Proxy proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
        return request.setProxy(proxy).setConnectionTimeout(sendTimeOut).setReadTimeout(readTimeout);
    }

    private static HttpRequest proxyRequest(HttpRequest request, Map<String, Object> map, int sendTimeOut, int readTimeout, Proxy.Type proxyType) {
        String proxyHost = MapUtil.getStr(map, "ip");
        int proxyPort = MapUtil.getInt(map, "port");
        String authUser = MapUtil.getStr(map, "account");
        String authPassword = proxyHost.equals("k786.kdltps.com") ? MapUtil.getStr(map, "pwd") + ":" + RandomUtil.randomNumbers(4) : MapUtil.getStr(map, "pwd");
        return proxyRequest(request, proxyHost, proxyPort, authUser, authPassword, sendTimeOut, readTimeout, proxyType);
    }

    private static HttpRequest proxyRequest(HttpRequest request, Integer sendTimeOut, Integer readTimeOut) {
        return request.setConnectionTimeout(sendTimeOut).setReadTimeout(readTimeOut);
    }

    private static Proxy.Type getProxyType(String type) {
        Proxy.Type proxyType;
        type = StrUtil.isEmpty(type) ? PropertiesUtil.getOtherConfig("proxyType", "") : type;
        if (type.equals(ProxyEnum.Type.Http.getKey())) {
            proxyType = Proxy.Type.HTTP;
        } else if (type.equals(ProxyEnum.Type.Socks4.getKey()) ||
                type.equals(ProxyEnum.Type.Socks4a.getKey()) ||
                type.equals(ProxyEnum.Type.Socks5.getKey()) ||
                type.equals(ProxyEnum.Type.Socks5h.getKey())) {
            proxyType = Proxy.Type.SOCKS;
        } else {
            proxyType = Proxy.Type.HTTP;
        }
        return proxyType;
    }

    public static void lock(HttpRequest request) {
        String url = getUrl(request);
        synchronized (getLock(url)) {
            Instant now = Instant.now();
            Instant lastRequestTime = uriTimestamps.get(url);
            if (lastRequestTime != null) {
                long elapsedTime = now.toEpochMilli() - lastRequestTime.toEpochMilli();
                if (elapsedTime < MIN_INTERVAL_MS) {
                    long waitTime = MIN_INTERVAL_MS - elapsedTime;
                    CountDownLatch latch = new CountDownLatch(1);
                    scheduler.schedule(() -> {
                        latch.countDown();
                    }, waitTime, TimeUnit.MILLISECONDS);
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            uriTimestamps.put(url, Instant.now());
        }
    }

    public static void unlock(HttpRequest request) {
        scheduler.schedule(() -> {
            uriTimestamps.remove(getUrl(request));
        }, MIN_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public static String getUrl(HttpRequest request) {
        String url = request.getUrl();
        url = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        return url;
    }

    private static Object getLock(String uri) {
        return uri.intern();
    }


}

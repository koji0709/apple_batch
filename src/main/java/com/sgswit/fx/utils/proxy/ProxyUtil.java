package com.sgswit.fx.utils.proxy;

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
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.common.UnavailableException;
import com.sgswit.fx.enums.ProxyEnum;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author DeZh
 * @title: ProxyUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2511:34
 */
public class ProxyUtil{

    private static final ConcurrentHashMap<String, Instant> uriTimestamps = new ConcurrentHashMap<>();

    private static final long MIN_INTERVAL_MS = 200;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static Map<String,Integer> map503Error=new HashMap<>(16);
    private static Map<String,Integer> mapIoError=new HashMap<>(16);


    public static HttpResponse execute(HttpRequest request){
        // 限制请求频率
        //lock(request);
        String requestId= MD5.create().digestHex(request.toString());
        //判断是否设置代理
        HttpRequest.closeCookie();
        HttpResponse httpResponse=null;
       try{
           httpResponse= createRequest(request).execute();
           if(Thread.currentThread().isInterrupted()){
               throw new ServiceException("请求失败：停止任务");
           }
           if(503==httpResponse.getStatus()){
               int randomInt= RandomUtil.randomInt(1,3);
               ThreadUtil.sleep(randomInt*500);
               Integer int503=map503Error.get(requestId);
               int failCount=1;
               if(null!=int503){
                   failCount=1+int503;
               }
               map503Error.put(requestId,failCount);
               if(failCount>40){
                   throw new UnavailableException();
               }
               return execute(request);
           }
       }catch (IORuntimeException e){
           boolean has =request.getUrl().contains("/WebObjects/MZFinance.woa/wa/redeemCodeSrv");
           if(has){
               throw new ServiceException("资源请求超时，发生未知错误");
           }else{
              //链接超时
               int randomInt= RandomUtil.randomInt(1,3);
               ThreadUtil.sleep(randomInt*500);
               int failCount=1;
               Integer intIo=mapIoError.get(requestId);
               if(null!=intIo){
                   failCount=1+intIo;
               }
               mapIoError.put(requestId,failCount);
               if(Integer.valueOf(failCount)>10){
                   throw new ServiceException("资源请求超时，请检查网络");
               }
               return execute(request);
           }
       }catch (HttpException e){
           //响应超时
           throw new ServiceException("服务端响应超时");
       } finally {
           map503Error.remove(requestId);
           mapIoError.remove(requestId);
       }
       return httpResponse;
    }


    private static HttpRequest createRequest(HttpRequest request){
        try {
            String proxyMode= PropertiesUtil.getOtherConfig("proxyMode");
            int sendTimeOut=PropertiesUtil.getOtherInt("sendTimeOut");
            sendTimeOut=sendTimeOut==0?60*1000:sendTimeOut*1000;
            if(StringUtils.isEmpty(proxyMode)){
                return proxyRequest(request,sendTimeOut);
            }else if(ProxyEnum.Mode.API.getKey().equals(proxyMode)){
                //判断是否为空
                String proxyApiUrl= PropertiesUtil.getOtherConfig("proxyApiUrl");
                String proxyApiUser= PropertiesUtil.getOtherConfig("proxyApiUser");
                String proxyApiPass= PropertiesUtil.getOtherConfig("proxyApiPass");
                boolean proxyApiNeedPass= PropertiesUtil.getOtherBool("proxyApiNeedPass",false);
                if(!StringUtils.isEmpty(proxyApiUrl)){
                    apiProxyRequest(request,proxyApiUrl,proxyApiUser,proxyApiPass, proxyApiNeedPass,sendTimeOut);
                }
                return request.timeout(sendTimeOut);
            }else if(ProxyEnum.Mode.TUNNEL.getKey().equals(proxyMode)){
                String address= PropertiesUtil.getOtherConfig("proxyTunnelAddress");
                String proxyHost=address.split(":")[0];
                int proxyPort=Integer.valueOf(address.split(":")[1]);
                String authUser=PropertiesUtil.getOtherConfig("proxyTunnelUser");
                String authPassword= PropertiesUtil.getOtherConfig("proxyTunnelPass");
                return proxyRequest(request,proxyHost,proxyPort,authUser,authPassword,sendTimeOut,getProxyType(""));
            }else if(ProxyEnum.Mode.DEFAULT.getKey().equals(proxyMode)){
                List<Map<String, Object>> proxyConfigList= DataUtil.getProxyConfig();
                if(null!=proxyConfigList && !proxyConfigList.isEmpty()){
                    Map<String, List<Map<String, Object>>> skuMap = proxyConfigList.stream().collect(Collectors.groupingBy(e->e.get("proxyType").toString()));
                    for (Map.Entry<String, List<Map<String, Object>>> entry : skuMap.entrySet()) {
                        // key=1-API代理，2-隧道道理，3-静态IP代理
                        String key = entry.getKey();
                        List<Map<String, Object>> mapList=entry.getValue();
                        int index= ThreadLocalRandom.current().nextInt(mapList.size()) % mapList.size();
                        Map<String,Object> map=mapList.get(index);
                        String proxyHost= MapUtil.getStr(map,"ip");
                        int proxyPort=MapUtil.getInt(map,"port");
                        String authUser=MapUtil.getStr(map,"account");
                        String authPassword= MapUtil.getStr(map,"pwd");
//                        int randomInt=7;
                        int randomInt=RandomUtil.randomInt(0,10);
                        int limit=8;
                        System.out.println(randomInt);
                        if(limit> randomInt&& randomInt>=0){
                            return proxyRequest(request,proxyHost,proxyPort,authUser,authPassword,sendTimeOut,Proxy.Type.HTTP);
                        }else if(randomInt>=limit){
                            Entity entity=ApiProxyUtil.getRandomIp();
                            if(null==entity){
                               return createRequest(request);
                            }
                            proxyHost=entity.getStr("ip");
                            proxyPort=entity.getInt("port");
                            String username=entity.getStr("username");
                            String pwd=entity.getStr("pwd");
                            String protocol_type=entity.getStr("protocol_type");
                            return  proxyRequest(request,proxyHost,proxyPort,username,pwd, sendTimeOut,Proxy.Type.SOCKS);
                        }else {
                            return proxyRequest(request,proxyHost,proxyPort,authUser,authPassword,sendTimeOut,getProxyType(""));
                        }
                    }
                    return proxyRequest(request,sendTimeOut);
                }else{
                    return proxyRequest(request,sendTimeOut);
                }
            }else{
                return proxyRequest(request,sendTimeOut);
            }
        }catch (Exception e){
            throw e;
        }
    }
    private static HttpRequest apiProxyRequest(HttpRequest request,String proxyApiUrl,String proxyApiUser,String proxyApiPass,Boolean proxyApiNeedPass,Integer sendTimeOut){
        if(Validator.isUrl(proxyApiUrl)){
            HttpResponse response=HttpUtil.createRequest(Method.GET,proxyApiUrl).execute();
            if(response.getStatus()==200){
                String body= response.body();
                List<String> lines= Arrays.asList(body.split("\r\n"));
                if(null!=lines && lines.size()>0){
                    if(!proxyApiNeedPass){
                        return getIpByTxt(lines,request,"","",sendTimeOut);
                    }else{
                        return getIpByTxt(lines,request,proxyApiUser,proxyApiPass,sendTimeOut);
                    }
                }
            }
            return request.timeout(sendTimeOut);
        }else{
            //本地模式
            List<String> lines= FileUtil.readLines(new File(proxyApiUrl), Charset.defaultCharset());
            if(null!=lines && lines.size()>0){
                if(!proxyApiNeedPass){
                    return getIpByTxt(lines,request,"","",sendTimeOut);
                }else{
                    return getIpByTxt(lines,request,proxyApiUser,proxyApiPass,sendTimeOut);
                }
            }
            return request.timeout(sendTimeOut);
        }
    }
    private static HttpRequest getIpByTxt(List<String> lines, HttpRequest request, String authUser, String authPassword, int sendTimeOut){
        int index= ThreadLocalRandom.current().nextInt(lines.size()) % lines.size();
        String [] arr=lines.get(index).split(":");
        String host= arr[0];
        int port=Integer.valueOf(arr[1]);
        return proxyRequest(request,host,port,authUser,authPassword,sendTimeOut,getProxyType(""));
    }
    private static HttpRequest proxyRequest(HttpRequest request,String proxyHost,Integer proxyPort,String authUser,String authPassword,Integer sendTimeOut,Proxy.Type proxyType){
        // 设置请求验证信息
        Authenticator.setDefault(new ProxyAuthenticator(authUser, authPassword));
        return  request.setProxy(new Proxy(proxyType,new InetSocketAddress(proxyHost, proxyPort))).timeout(sendTimeOut);
    }
    private static HttpRequest proxyRequest(HttpRequest request,Integer sendTimeOut){
        return request.timeout(sendTimeOut);
    }
    private static Proxy.Type getProxyType(String type){
        Proxy.Type proxyType;
        type= StrUtil.isEmpty(type)? PropertiesUtil.getOtherConfig("proxyType",""):type;
        if(type.equals(ProxyEnum.Type.Http.getKey())){
            proxyType=Proxy.Type.HTTP;
        }else if(type.equals(ProxyEnum.Type.Socks4.getKey()) ||
                type.equals(ProxyEnum.Type.Socks4a.getKey()) ||
                type.equals(ProxyEnum.Type.Socks5.getKey()) ||
                type.equals(ProxyEnum.Type.Socks5h.getKey())){
            proxyType=Proxy.Type.SOCKS;
        }else{
            proxyType=Proxy.Type.HTTP;
        }
        return proxyType;
    }

    public static void lock(HttpRequest request){
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

    public static void unlock(HttpRequest request){
        scheduler.schedule(() -> {
            uriTimestamps.remove(getUrl(request));
        }, MIN_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public static String getUrl(HttpRequest request){
        String  url = request.getUrl();
        url = url.contains("?") ? url.substring(0,url.indexOf("?")) : url;
        return url;
    }

    private static Object getLock(String uri) {
        return uri.intern();
    }

}

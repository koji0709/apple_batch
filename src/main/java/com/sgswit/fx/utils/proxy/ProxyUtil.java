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
import com.sgswit.fx.utils.*;
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
    private static Map<String,Integer> tryNumMap=new HashMap<>(16);


    public static HttpResponse execute(HttpRequest request){
        // 限制请求频率
        //lock(request);

        String requestId= MD5.create().digestHex(request.toString());
        //判断是否设置代理
        HttpRequest.closeCookie();
        HttpResponse httpResponse=null;
        boolean has =request.getUrl().contains("/WebObjects/MZFinance.woa/wa/redeemCodeSrv");
        boolean iTunesFlag =request.getUrl().contains("/WebObjects/MZFinance.woa");
        int sleepTime=iTunesFlag?200:500;
        int try503Num=has?20:50;
        int tryIoNum=10;
        try{
            httpResponse=  createRequest(request).execute();
            if(Thread.currentThread().isInterrupted()){
                throw new ServiceException("请求失败：停止任务");
            }
            if(503==httpResponse.getStatus()){
               int randomInt= RandomUtil.randomInt(1,3);
               ThreadUtil.sleep(randomInt*sleepTime);
               Integer int503=map503Error.get(requestId);
               int failCount=1;
               if(null!=int503){
                   failCount=1+int503;
               }
               map503Error.put(requestId,failCount);
               if(failCount>try503Num){
                   throw new UnavailableException();
               }
               return execute(request);
            }
       }catch (UnavailableException e){
            throw e;
       }catch (IORuntimeException | HttpException e){
            LoggerManger.info("网络异常",e);
            if (StringUtils.containsIgnoreCase(e.getMessage(),"connect") ||
                    StringUtils.containsIgnoreCase(e.getMessage(),"connection")||
                    StringUtils.containsIgnoreCase(e.getMessage(),"503 Service Unavailable")||
                    StringUtils.containsIgnoreCase(e.getMessage(),"407 Proxy Authentication Required")||
                    StringUtils.containsIgnoreCase(e.getMessage(),"authentication failed")||
                    StringUtils.containsIgnoreCase(e.getMessage(),"Remote host terminated the handshake")||
                    StringUtils.containsIgnoreCase(e.getMessage(),"SOCKS: Network unreachable")||
                    StringUtils.containsIgnoreCase(e.getMessage(),"460 Proxy Authentication Invalid")
            ){
                int randomInt= RandomUtil.randomInt(1,3);
                ThreadUtil.sleep(randomInt*sleepTime);
                int failCount=1;
                Integer intIo=mapIoError.get(requestId);
                if(null!=intIo){
                    failCount=1+intIo;
                }
                mapIoError.put(requestId,failCount);
                if(Integer.valueOf(failCount)>=tryIoNum){
                    throw new ServiceException("网络连接失败，请稍后重试");
                }
                return execute(request);
            }else if(StringUtils.containsIgnoreCase(e.getMessage(),"read")){
                if(has){
                    throw new ServiceException("网络连接失败，未知状态");
                }else{
                    throw new ServiceException("服务响应超时，请稍后重试");
                }
            }else{
                e.printStackTrace();
                 throw new ServiceException("网络连接异常，请稍后重试");
            }
       }catch (ServiceException e){
            throw e;
       }catch (Exception e){
            // 捕获异常
            throw new ServiceException("网络连接异常，未知错误");
       } finally {
           map503Error.remove(requestId);
           mapIoError.remove(requestId);
       }
       return httpResponse;
    }

    private static HttpRequest createRequest(HttpRequest request){
        String requestId= MD5.create().digestHex(request.toString());
        try {
            String proxyMode= PropertiesUtil.getOtherConfig("proxyMode");
            int sendTimeOut=PropertiesUtil.getOtherInt("sendTimeOut");
            sendTimeOut=sendTimeOut==0?10*1000:sendTimeOut*1000;
            int readTimeOut=30*1000;
            if(StringUtils.isEmpty(proxyMode)){
                return proxyRequest(request,sendTimeOut,readTimeOut);
            }else if(ProxyEnum.Mode.API.getKey().equals(proxyMode)){
                //判断是否为空
                String proxyApiUrl= PropertiesUtil.getOtherConfig("proxyApiUrl");
                String proxyApiUser= PropertiesUtil.getOtherConfig("proxyApiUser");
                String proxyApiPass= PropertiesUtil.getOtherConfig("proxyApiPass");
                boolean proxyApiNeedPass= PropertiesUtil.getOtherBool("proxyApiNeedPass",false);
                if(!StringUtils.isEmpty(proxyApiUrl)){
                    apiProxyRequest(request,proxyApiUrl,proxyApiUser,proxyApiPass, proxyApiNeedPass,sendTimeOut,readTimeOut);
                }
                return proxyRequest(request,sendTimeOut,readTimeOut);
            }else if(ProxyEnum.Mode.TUNNEL.getKey().equals(proxyMode)){
                String address= PropertiesUtil.getOtherConfig("proxyTunnelAddress");
                String proxyHost=address.split(":")[0];
                int proxyPort=Integer.valueOf(address.split(":")[1]);
                String authUser=PropertiesUtil.getOtherConfig("proxyTunnelUser");
                String authPassword= PropertiesUtil.getOtherConfig("proxyTunnelPass");
                return proxyRequest(request,proxyHost,proxyPort,authUser,authPassword,sendTimeOut,readTimeOut,getProxyType(""));
            }else if(ProxyEnum.Mode.DEFAULT.getKey().equals(proxyMode)){
                List<Map<String, Object>> proxyConfigList= DataUtil.getProxyConfig();
                if(null!=proxyConfigList && !proxyConfigList.isEmpty()){
                    //根据权重配比，随机获取一种
                    int[] weights=new int[proxyConfigList.size()];
                    int i=0;
                    for (Map<String, Object> map:proxyConfigList){
                        weights[i]=MapUtil.getInt(map,"weight");
                        i++;
                    }
                    int index= StrUtils.getWeightedRandomIndex(weights);
                    Map<String, Object> proxyConfigMap= proxyConfigList.get(index);
                    String proxyType=MapUtil.getStr(proxyConfigMap,"proxyType");

                    // todo

                    if("1".equals(proxyType)){
                        Entity entity=ApiProxyUtil.getRandomIp();
                        if(null==entity){
                            int failCount=1;
                            Integer intIo=tryNumMap.get(requestId);
                            if(null!=intIo){
                                failCount=1+intIo;
                            }
                            tryNumMap.put(requestId,failCount);
                            if(Integer.valueOf(failCount)>=10){
                                throw new ServiceException("网络连接失败，请稍后重试");
                            }
                            return createRequest(request);
                        }else{
                            String proxyHost=entity.getStr("ip");
                            int proxyPort=entity.getInt("port");
                            String username=entity.getStr("username");
                            String pwd=entity.getStr("pwd");
                            String protocol_type=entity.getStr("protocol_type");
                            try{
                                username= AesUtil.decrypt(username);
                            }catch (Exception e){

                            }
                            try{
                                pwd= AesUtil.decrypt(pwd);
                            }catch (Exception e){

                            }
                            return  proxyRequest(request,proxyHost,proxyPort,username,pwd, sendTimeOut,readTimeOut,getProxyType(protocol_type));
                        }
                    }else if("2".equals(proxyType)){
                        String protocolType=MapUtil.getStr(proxyConfigMap,"protocolType");
                        String authUser=MapUtil.getStr(proxyConfigMap,"account");
                        try{
                            authUser= AesUtil.decrypt(authUser);
                        }catch (Exception e){

                        }
                        String authPassword= MapUtil.getStr(proxyConfigMap,"pwd");
                        try{
                            authPassword= AesUtil.decrypt(authPassword);
                        }catch (Exception e){

                        }
                        proxyConfigMap.put("account",authUser);
                        proxyConfigMap.put("pwd",authPassword);
                        return proxyRequest(request,proxyConfigMap,sendTimeOut,readTimeOut,getProxyType(protocolType));
                    }else {
                        return proxyRequest(request,sendTimeOut,readTimeOut);
                    }
                }else {
                    return proxyRequest(request,sendTimeOut,readTimeOut);
                }
            }else{
                return proxyRequest(request,sendTimeOut,readTimeOut);
            }
        }catch (Exception e){
            throw e;
        }finally {
            tryNumMap.remove(requestId);
        }
    }
    private static HttpRequest apiProxyRequest(HttpRequest request,String proxyApiUrl,String proxyApiUser,String proxyApiPass,Boolean proxyApiNeedPass,int sendTimeOut,int readTimeout){
        if(Validator.isUrl(proxyApiUrl)){
            HttpResponse response=HttpUtil.createRequest(Method.GET,proxyApiUrl).execute();
            if(response.getStatus()==200){
                String body= response.body();
                List<String> lines= Arrays.asList(body.split("\r\n"));
                if(null!=lines && lines.size()>0){
                    if(!proxyApiNeedPass){
                        return getIpByTxt(lines,request,"","",sendTimeOut,readTimeout);
                    }else{
                        return getIpByTxt(lines,request,proxyApiUser,proxyApiPass,sendTimeOut,readTimeout);
                    }
                }
            }
            return request.timeout(sendTimeOut);
        }else{
            //本地模式
            List<String> lines= FileUtil.readLines(new File(proxyApiUrl), Charset.defaultCharset());
            if(null!=lines && lines.size()>0){
                if(!proxyApiNeedPass){
                    return getIpByTxt(lines,request,"","",sendTimeOut,readTimeout);
                }else{
                    return getIpByTxt(lines,request,proxyApiUser,proxyApiPass,sendTimeOut,readTimeout);
                }
            }
            return request.timeout(sendTimeOut);
        }
    }
    private static HttpRequest getIpByTxt(List<String> lines, HttpRequest request, String authUser, String authPassword, int sendTimeOut,int readTimeout){
        int index= ThreadLocalRandom.current().nextInt(lines.size()) % lines.size();
        String [] arr=lines.get(index).split(":");
        String host= arr[0];
        int port=Integer.valueOf(arr[1]);
        return proxyRequest(request,host,port,authUser,authPassword,sendTimeOut,readTimeout,getProxyType(""));
    }
    private static HttpRequest proxyRequest(HttpRequest request,String proxyHost,Integer proxyPort,String authUser,String authPassword,int sendTimeOut,int readTimeout,Proxy.Type proxyType){
        LoggerManger.info(String.format("uri = %s, proxy = %s:%d",request.getUrl(),proxyHost,proxyPort));
        // 设置请求验证信息
        Authenticator.setDefault(new ProxyAuthenticator(authUser, authPassword));
        Proxy proxy= new Proxy(proxyType,new InetSocketAddress(proxyHost, proxyPort));
        return  request.setProxy(proxy).setConnectionTimeout(sendTimeOut).setReadTimeout(readTimeout);
    }
    private static HttpRequest proxyRequest(HttpRequest request,Map<String,Object> map,int sendTimeOut,int readTimeout,Proxy.Type proxyType){
        String proxyHost= MapUtil.getStr(map,"ip");
        int proxyPort=MapUtil.getInt(map,"port");
        String authUser=MapUtil.getStr(map,"account");
        String authPassword= MapUtil.getStr(map,"pwd");
        return proxyRequest(request,proxyHost,proxyPort,authUser,authPassword,sendTimeOut,readTimeout,proxyType);
    }
    private static HttpRequest proxyRequest(HttpRequest request,Integer sendTimeOut,Integer readTimeOut){
        return request.setConnectionTimeout(sendTimeOut).setReadTimeout(readTimeOut);
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

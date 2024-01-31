package com.sgswit.fx.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.sgswit.fx.enums.ProxyEnum;
import org.apache.commons.lang3.StringUtils;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author DeZh
 * @title: ProxyUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2511:34
 */
public class ProxyUtil{
    public static HttpRequest createGet(String url) {
        return createRequest(Method.GET,url);
    }
    public static HttpRequest createPost(String url) {
        return createRequest(Method.POST,url);
    }

    public static HttpRequest createRequest(Method method,String url){
        //判断是否设置代理
        try {
            String proxyMode=PropertiesUtil.getOtherConfig("proxyMode");
            int sendTimeOut=PropertiesUtil.getOtherInt("sendTimeOut");
            sendTimeOut=sendTimeOut==0?30*1000:sendTimeOut*1000;
            if(StringUtils.isEmpty(proxyMode)){
                return HttpUtil.createRequest(method,url).setConnectionTimeout(sendTimeOut);
            }else if(ProxyEnum.Mode.API.getKey().equals(proxyMode)){
                return HttpUtil.createRequest(method,url).setConnectionTimeout(sendTimeOut);
            }else if(ProxyEnum.Mode.TUNNEL.getKey().equals(proxyMode)){
                String address= PropertiesUtil.getOtherConfig("proxyTunnelAddress");
                String host=address.split(":")[0];
                int port=Integer.valueOf(address.split(":")[1]);
                String authUser=PropertiesUtil.getOtherConfig("proxyTunnelUser");
                String authPassword= PropertiesUtil.getOtherConfig("proxyTunnelPass");
                return proxyRequest(method,url,host,port,authUser,authPassword,sendTimeOut);
            }else if(ProxyEnum.Mode.DEFAULT.getKey().equals(proxyMode)){
                List<Map<String, Object>> proxyConfig= DataUtil.getProxyConfig();
                if(null!=proxyConfig && !proxyConfig.isEmpty()){
                    int index= ThreadLocalRandom.current().nextInt(proxyConfig.size()) % proxyConfig.size();
                    Map<String,Object> map=proxyConfig.get(index);
                    String host= MapUtil.getStr(map,"ip");
                    String authUser= MapUtil.getStr(map,"account");
                    String authPassword= MapUtil.getStr(map,"pwd");
                    int port=MapUtil.getInt(map,"port");
                    return proxyRequest(method,url,host,port,authUser,authPassword,sendTimeOut);
                }else{
                    return HttpUtil.createRequest(method,url).setConnectionTimeout(sendTimeOut);
                }
            }else{
                return HttpUtil.createRequest(method,url).setConnectionTimeout(sendTimeOut);
            }
        }catch (Exception e){

        }
        return null;
    }
   private static HttpRequest proxyRequest(Method method,String url,String host,Integer port,String authUser,String authPassword,Integer sendTimeOut){
       Authenticator.setDefault(
               new Authenticator() {
                   @Override
                   public PasswordAuthentication getPasswordAuthentication() {
                       return new PasswordAuthentication(authUser, authPassword.toCharArray());
                   }
               }
       );
       Proxy.Type proxyType;
       String type=PropertiesUtil.getOtherConfig("proxyType","");
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
       return  HttpUtil.createRequest(method,url).setProxy(new Proxy(proxyType,
               new InetSocketAddress(host, port))).setConnectionTimeout(sendTimeOut);

   }





}

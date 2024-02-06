package com.sgswit.fx.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.sgswit.fx.enums.ProxyEnum;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.Arrays;
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
                //判断是否为空
                String proxyApiUrl= PropertiesUtil.getOtherConfig("proxyApiUrl");
                String proxyApiUser= PropertiesUtil.getOtherConfig("proxyApiUser");
                String proxyApiPass= PropertiesUtil.getOtherConfig("proxyApiPass");
                boolean proxyApiNeedPass= PropertiesUtil.getOtherBool("proxyApiNeedPass",false);
                if(!StringUtils.isEmpty(proxyApiUrl)){
                    if(Validator.isUrl(proxyApiUrl)){
                        HttpResponse response=HttpUtil.createRequest(Method.GET,proxyApiUrl).execute();
                        if(response.getStatus()==200){
                           String body= response.body();
                            List<String> lines= Arrays.asList(body.split("\r\n"));
                            if(null!=lines && lines.size()>0){
                                if(!proxyApiNeedPass){
                                    return getIpByTxt(lines,method,"","",url,sendTimeOut);
                                }else{
                                    return getIpByTxt(lines,method,proxyApiUser,proxyApiPass,url,sendTimeOut);
                                }
                            }

                        }
                    }else{
                       List<String> lines=FileUtil.readLines(new File(proxyApiUrl), Charset.defaultCharset());
                       if(null!=lines && lines.size()>0){
                           if(!proxyApiNeedPass){
                               return getIpByTxt(lines,method,"","",url,sendTimeOut);
                           }else{
                               return getIpByTxt(lines,method,proxyApiUser,proxyApiPass,url,sendTimeOut);
                           }
                       }
                    }
                }

                //判断是否为url


//                Validator.isUrl();





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

       return  HttpUtil.createRequest(method,url).setProxy(new Proxy(getProxyType(),new InetSocketAddress(host, port))).setConnectionTimeout(sendTimeOut);

   }

  private static Proxy.Type getProxyType(){
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
        return proxyType;
    }

    private static HttpRequest getIpByTxt(List<String> lines,Method method,String authUser,String authPassword,String url,int sendTimeOut){
        int index= ThreadLocalRandom.current().nextInt(lines.size()) % lines.size();
        String [] arr=lines.get(index).split(":");
        String host= arr[0];
        int port=Integer.valueOf(arr[1]);
        return proxyRequest(method,url,host,port,authUser,authPassword,sendTimeOut);
    }


}

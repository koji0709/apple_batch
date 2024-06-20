package com.sgswit.fx.utils.proxy;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author DeZh
 * @title: ProxyUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2511:34
 */
public class ProxyUtil{

    public static HttpResponse execute(HttpRequest request){
        //判断是否设置代理
        HttpRequest.closeCookie();
       HttpResponse httpResponse=null;
       try{
           httpResponse= createRequest(request).execute();
           if(503==httpResponse.getStatus()){
               ThreadUtil.sleep(1000);
               String failCountStr=request.header("fc503");
               if(StrUtil.isEmpty(failCountStr)){
                   failCountStr="1";
               }else{
                   failCountStr=String.valueOf(Integer.valueOf(failCountStr)+1);
               }
               request.header("fc503", failCountStr);
               if(Integer.valueOf(failCountStr)>20){
                   throw new UnavailableException();
               }
               return execute(request);
           }
       }catch (IORuntimeException e){
           //链接超时
           ThreadUtil.sleep(1000);
           String failCountStr=request.header("fc");
           if(StrUtil.isEmpty(failCountStr)){
               failCountStr="1";
           }else{
               failCountStr=String.valueOf(Integer.valueOf(failCountStr)+1);
           }
           request.header("fc", failCountStr);
           if(Integer.valueOf(failCountStr)>5){
             throw new ServiceException("网络异常");
           }
           return execute(request);
       }catch (HttpException e){
           //响应超时
           throw new ServiceException("网络异常");
       }

       return httpResponse;
    }
    private static HttpRequest createRequest(HttpRequest request){
        try {
            String proxyMode= PropertiesUtil.getOtherConfig("proxyMode");
            int sendTimeOut=PropertiesUtil.getOtherInt("sendTimeOut");
            sendTimeOut=sendTimeOut==0?30*1000:sendTimeOut*1000;
//            Entity entity=ApiProxyUtil.getRandomIp();
//            String proxyHost=entity.getStr("host");
//            int proxyPort=entity.getInt("port");
//            return  proxyRequest(request,proxyHost,proxyPort,ApiProxyUtil.ProxyUser,ApiProxyUtil.ProxyPass, sendTimeOut);
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
                return proxyRequest(request,proxyHost,proxyPort,authUser,authPassword,sendTimeOut);
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
                        if(key.equals("2")){
                            return proxyRequest(request,proxyHost,proxyPort,authUser,authPassword,sendTimeOut);
                        }else if(key.equals("1")){
                            String proxyApiUrl= MessageFormat.format("{0}:{1}",new String[]{proxyHost, String.valueOf(proxyPort)});
                            return  apiProxyRequest(request,proxyApiUrl,authUser,authPassword, false,sendTimeOut);
                        }else {
                            return proxyRequest(request,proxyHost,proxyPort,authUser,authPassword,sendTimeOut);
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
        return proxyRequest(request,host,port,authUser,authPassword,sendTimeOut);
    }
    private static HttpRequest proxyRequest(HttpRequest request,String proxyHost,Integer proxyPort,String authUser,String authPassword,Integer sendTimeOut){
        // 设置请求验证信息
        Authenticator.setDefault(new ProxyAuthenticator(authUser, authPassword));
        return  request.setProxy(new Proxy(getProxyType(),new InetSocketAddress(proxyHost, proxyPort))).timeout(sendTimeOut);
    }
    private static HttpRequest proxyRequest(HttpRequest request,Integer sendTimeOut){
        return request.timeout(sendTimeOut);
    }
    private static Proxy.Type getProxyType(){
        Proxy.Type proxyType;
        String type= PropertiesUtil.getOtherConfig("proxyType","");
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


}

package com.sgswit.fx.utils;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.iTunes.vo.AppstoreDownloadVo;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.LoginInfo;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * iTunes工具类
 * @author DeZh
 * @title: T
 * @projectName appleBatch
 * @date 2023/9/2720:32
 */
public class ITunesUtil {

    public  static HttpResponse getPurchases(HttpResponse response){
        HashMap<String, List<String>> headers = new HashMap<>();
//
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("p30-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));

//        headers.put("User-Agent",ListUtil.toList("iTunes/12.12.10 (Windows; Microsoft Windows 10 x64 (Build 19045); x64) AppleWebKit/7613.2007.1014.14 (dt:2)"));
        headers.put("User-Agent", ListUtil.toList("music/2.0 (Macintosh; OS X 12.10) AppleWebKit/600.1.3.41"));
        headers.put("X-Apple-I-MD-RINFO",ListUtil.toList("143465-19,32"));
        headers.put("X-Dsid",ListUtil.toList("8135448658"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("X-Token",ListUtil.toList("17F0A7C9295E7B44BA41A2F39E8626AA"));

        String cookie=CookieUtils.getCookiesFromHeader(response);

        HttpResponse step4Res = HttpUtil.createRequest(Method.GET,"https://p30-buy.itunes.apple.com/commerce/account/purchases?isJsonApiFormat=true&page=1")
                .header(headers)
//                .body(body)
                .cookie(cookie)
                .execute();

        System.out.println(step4Res.body());
        return step4Res;
    }
    /**
     　* 统计购买记录
     * @param
     * @param response
    　* @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
    　* @throws
    　* @author DeZh
    　* @date 2023/10/19 10:09
     */
    public  static List<Map<String,String>> accountPurchasesCount(HttpResponse response){
        List<Map<String,String>> result=new ArrayList<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("p30-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));
        headers.put("Origin", ListUtil.toList("https://finance-app.itunes.apple.com"));

        headers.put("User-Agent",ListUtil.toList("iTunes/12.13 (Windows; Microsoft Windows 10 x64 (Build 19045); x64) AppleWebKit/7613.2007.1014.14 (dt:2)"));
        headers.put("X-Dsid",ListUtil.toList("8135448658"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("X-Token",ListUtil.toList("AwIAAAECAAHZ1AAAAABlbuYKP7hYlRFLTKQGuqllgT8SVh9Ca0w="));
        String cookie=CookieUtils.getCookiesFromHeader(response);
        HttpResponse step4Res = HttpUtil.createRequest(Method.GET,"https://p30-buy.itunes.apple.com/commerce/account/purchases/count?isDeepLink=false&isJsonApiFormat=true&page=1")
                .header(headers)
                .cookie(cookie)
                .execute();
        if(step4Res.getStatus()==200){
            String years=JSONUtil.parseObj(step4Res.body()).getByPath("data.attributes.dates.years").toString();
            JSONArray jsonArray=  JSONUtil.parseArray(years);
            for(Object object:jsonArray){
                JSONObject jsonObject=JSONUtil.parseObj(object.toString());
                String key= (String) jsonObject.keySet().toArray()[0];
                String value=jsonObject.getByPath(key+".items").toString();
                result.add(new HashMap<>(){{
                    put(key,value);
                }});
            }
        }
        return result;
    }
    /**
     　*获取支付方式（iTunes版）
     * @param
     * @param paras
    　* @return java.util.Map<java.lang.String,java.lang.Object>
    　* @throws
    　* @author DeZh
    　* @date 2023/12/10 10:57
     */
    public  static HttpResponse getPaymentInfos(Map<String,Object> paras){
        Map<String,Object> result=new HashMap<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Content-Type", ListUtil.toList("application/json; charset=UTF-8"));
        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));
        headers.put("Origin", ListUtil.toList("https://finance-app.itunes.apple.com"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("X-Token",ListUtil.toList(paras.get("passwordToken").toString()));
        String cookies = MapUtil.getStr(paras,"cookies","");

        HttpResponse httpResponse = HttpUtil.createRequest(Method.GET,"https://p"+paras.get("itspod")+"-buy.itunes.apple.com/account/stackable/paymentInfos?managePayments=true")
                .header(headers)
                .cookie(cookies)
                .execute();
        return httpResponse;
    }


    /**
     　* 删除所有支付方式（iTunes版）
     * @param
     * @param paras
    　* @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
    　* @throws
    　* @author DeZh
    　* @date 2023/10/25 11:49
     */
    public  static Map<String,Object> delPaymentInfos(Map<String,Object> paras){
        Map<String,Object> result=new HashMap<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Content-Type", ListUtil.toList("application/json; charset=UTF-8"));
        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));
        headers.put("Origin", ListUtil.toList("https://finance-app.itunes.apple.com"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("X-Token",ListUtil.toList(paras.get("passwordToken").toString()));
        String cookies = MapUtil.getStr(paras,"cookies","");
        //获取支付方式
        HttpResponse httpResponse=getPaymentInfos(paras);
        if(httpResponse.getStatus()==401){
            result.put("code","1");
            result.put("msg","未登录或登录超时");
        }else if(httpResponse.getStatus()==200){
            String paymentInfosJsonString= JSONUtil.parseObj(httpResponse.body()).getByPath("data.attributes.paymentInfos").toString();
            JSONArray jsonArray=JSONUtil.parseArray(paymentInfosJsonString);
            boolean hasPayment=false;
            for(Object jsonObject:jsonArray){
                String paymentId= (String) ((JSONObject)jsonObject).getByPath("paymentId");
                String paymentMethodType= (String) ((JSONObject)jsonObject).getByPath("paymentMethodType");
                if(!"None".equalsIgnoreCase(paymentMethodType)){
                    hasPayment=true;
                    String url= MessageFormat.format("https://p"+paras.get("itspod")+"-buy.itunes.apple.com/account/stackable/paymentInfos/{0}/delete",paymentId);
                    HttpResponse delHttpResponse = HttpUtil.createRequest(Method.POST,url)
                            .header(headers)
                            .cookie(cookies)
                            .execute();
                }
            }
            if(hasPayment){
                result.put("code",Constant.SUCCESS);
                result.put("msg","操作成功");
            }else{
                result.put("code",Constant.SUCCESS);
                result.put("msg","无付款方式");
            }
        }
        return result;
    }
    /**
     　* 添加信用卡支付方式
     * @param
     * @param paras
     * @param step 01-发送短信验证码，02-提交信息
    　* @return java.util.Map<java.lang.String,java.lang.String>
    　* @throws
    　* @author DeZh
    　* @date 2023/10/30 9:27
     */
    public  static Map<String,Object> addCreditPayment(Map<String,Object> paras,String step){
        Map<String,Object> result=new HashMap<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded; charset=UTF-8"));
        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));
        headers.put("Origin", ListUtil.toList("https://finance-app.itunes.apple.com"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));

        headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
        headers.put("X-Token",ListUtil.toList(paras.get("passwordToken").toString()));
        String paymentInfoResponseBody="";
        if("01".equals(step)){
            HttpResponse httpResponse=getPaymentInfos(paras);
            if(httpResponse.getStatus()==401){
                result.put("code","1");
                result.put("message","未登录或登录超时");
                return result;
            }else if(httpResponse.getStatus()==200){
                paymentInfoResponseBody=httpResponse.body();
                paras.put("paymentInfoResponseBody",paymentInfoResponseBody);
            }
        }else if("02".equals(step)){
            paymentInfoResponseBody=MapUtil.getStr(paras,"paymentInfoResponseBody");
        }
        Map<String, String> source=new HashMap<>();
        String paymentInfosStr=JSONUtil.parse(paymentInfoResponseBody).getByPath("data.attributes.paymentInfos",String.class);
        JSON paymentInfo=null;
        String ccSubType=null;
        for(Object paymentInfoObj:JSONUtil.parseArray(paymentInfosStr)){
            paymentInfo=JSONUtil.parse(paymentInfoObj);
            String cc=paymentInfo.getByPath("ccSubType",String.class);
            if(!StringUtils.isEmpty(cc)){
                ccSubType=cc;
            }
        }
        String phoneOfficeNumber=paymentInfo.getByPath("phone.phoneOfficeNumber",String.class);
        source=JSONUtil.toBean(paymentInfo.getByPath("billingAddress",String.class),Map.class);
        source.put("phoneOfficeNumber",phoneOfficeNumber);
        source.put("iso3CountryCode",source.get("addressOfficialCountryCode"));

        //支付信息
        source.put("paymentMethodType","CreditCard");
        if(!StringUtils.isEmpty(ccSubType)){
            source.put("ccSubType",ccSubType);
        }
        source.put("paymentMethodVersion","2.0");
        source.put("needsTopUp","false");
        source.put("isCameraInput","false");
        source.put("creditCardNumber",MapUtil.getStr(paras,"creditCardNumber",""));
        source.put("creditCardType","UPCC");
        source.put("creditCardExpirationMonth",MapUtil.getStr(paras,"creditCardExpirationMonth",""));
        source.put("creditCardExpirationYear",MapUtil.getStr(paras,"creditCardExpirationYear",""));
        source.put("creditVerificationNumber",MapUtil.getStr(paras,"creditVerificationNumber",""));
        source.put("paymentMethodType","CreditCard");
        if("02".equals(step)){
            source.put("liteSessionId",MapUtil.getStr(paras,"liteSessionId",""));
            source.put("transactionId",MapUtil.getStr(paras,"transactionId",""));
            source.put("smsCode",MapUtil.getStr(paras,"smsCode",""));
        }
        //转为url参数格式的字符串
        String body=MapUtil.join(source,"&","=",false);
        String guid=MapUtil.getStr(paras,"guid","");
        HttpResponse response = HttpUtil.createRequest(Method.POST,"https://p"+paras.get("itspod")+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/addOrEditBillingInfoSrv?guid="+guid)
                .header(headers)
                .cookie(MapUtil.getStr(paras,"cookies",""))
                .body(body)
                .execute();
        if(response.getStatus()==401){
            result.put("code","-1");
            result.put("message","未登录或登录超时");
        }else if(response.getStatus()==200){
            JSON bodyJson= JSONUtil.parse(response.body());
            int status=bodyJson.getByPath("status",int.class);
            result.put("data",paras);
            result.put("code",Constant.SUCCESS);
            if(status==0){
                if("01".equals(step)){
                    String liteSessionId= bodyJson.getByPath("result.liteSessionId",String.class);
                    String transactionId= bodyJson.getByPath("result.transactionId",String.class);
                    paras.put("liteSessionId",liteSessionId);
                    paras.put("transactionId",transactionId);
                    result.put("code",Constant.SUCCESS);
                    result.put("message","请输入发送至手机【"+phoneOfficeNumber+"】的银联验证码");
                    result.put("data",paras);
                }else{
                    result.put("code",Constant.SUCCESS);
                    result.put("message","添加成功");
                }
            }else{
                StringBuffer stringBuffer=new StringBuffer();
                String validationResults= bodyJson.getByPath("result.validationResults",String.class);
                if(!StringUtils.isEmpty(validationResults)){
                    JSONArray jsonArray=JSONUtil.parseArray(validationResults);
                    for(Object jsonObject:jsonArray){
                        String validationRuleName=JSONUtil.parse(jsonObject).getByPath("errorString",String.class);
                        switch (validationRuleName){
                            case "INVALID_PHONE_NUMBER":
                                stringBuffer.append("手机号码不正确，请更新并重试。");
                                stringBuffer.append("\n");
                                break;
                            default:
                                String errorString= JSONUtil.parse(jsonObject).getByPath("errorString",String.class);
                                stringBuffer.append(errorString);
                                stringBuffer.append("\n");
                        }
                    }
                }else if(!StringUtils.isEmpty(bodyJson.getByPath("errorMessageKey",String.class))){
                    String userPresentableErrorMessage= bodyJson.getByPath("userPresentableErrorMessage",String.class);
                    stringBuffer.append(userPresentableErrorMessage);
                }
                result.put("code","-1");
                result.put("message",stringBuffer.toString());
            }
        }
        return result;
    }

    /**
    　* 判断账户是否需要实名认证
      * @param
     * @param paras
    　* @return boolean
    　* @throws
    　* @author DeZh
    　* @date 2024/1/29 17:25
    */
    public static boolean redeemLandingPage(Map<String,Object> paras){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("X-Apple-Store-Front",ListUtil.toList(MapUtil.getStr(paras,"storeFront")));
        headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
        headers.put("X-Token",ListUtil.toList(paras.get("passwordToken").toString()));
        headers.put("X-Apple-Client-Application",ListUtil.toList("Software"));
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Content-Type", ListUtil.toList("text/html"));
        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("User-Agent",ListUtil.toList("MacAppStore/2.0 (Macintosh; OS X 12.10) AppleWebKit/600.1.3.41"));
        String cookies = MapUtil.getStr(paras,"cookies","");
        //获取支付方式
        String url="https://p"+paras.get("itspod")+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/redeemLandingPage?cc=cn";
        HttpResponse response = HttpUtil.createRequest(Method.GET,url)
                .header(headers)
                .cookie(cookies)
                .execute();
        Document document= Jsoup.parse(response.body());
        Element element=document.getElementById("nationalIdForm");
        String collectNationalId=element.attr("collect-national-id");
        return Boolean.valueOf(collectNationalId);
    }

    public static Map<String,Object> redeemValidateId(Map<String,Object> paras){
        Map<String,Object> result=new HashMap<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("X-Apple-Store-Front",ListUtil.toList(MapUtil.getStr(paras,"storeFront")));
        headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
        headers.put("X-Token",ListUtil.toList(paras.get("passwordToken").toString()));
        headers.put("X-Apple-Client-Application",ListUtil.toList("Software"));
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded; charset=UTF-8"));
        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("User-Agent",ListUtil.toList("MacAppStore/2.0 (Macintosh; OS X 12.10) AppleWebKit/600.1.3.41"));
        String cookies = MapUtil.getStr(paras,"cookies","");
        //获取支付方式
        Map<String, String> source=new HashMap<>();
        source.put("response-content-type", "application/json");
        source.put("name", MapUtil.getStr(paras,"name"));
        source.put("phone", MapUtil.getStr(paras,"phone"));
        source.put("nationalId", MapUtil.getStr(paras,"nationalId"));
        String body=MapUtil.join(source,"&","=",false);
        String url="https://p"+paras.get("itspod")+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/redeemValidateId?"+body;
        HttpResponse response = HttpUtil.createRequest(Method.POST,url)
                .header(headers)
                .cookie(cookies)
                .execute();
        System.out.println(response.getStatus());
        System.out.println(response.body());
        if(200!=response.getStatus()){
            result.put("code","1");
            result.put("msg","认证失败，未知错误");
        }else{
            JSON bodyJson= JSONUtil.parse(response.body());
            int status=bodyJson.getByPath("status",int.class);
            if(status!=0){
                result.put("code","1");
                result.put("msg",bodyJson.getByPath("userPresentableErrorMessage"));
            }else {
                result.put("code",Constant.SUCCESS);
                result.put("msg","认证成功");
            }
        }
        return result;
    }



    public static Map<String,Object> appStoreOverCheck(Map<String,Object> paras){
        Map<String,Object> result=new HashMap<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Content-Type", ListUtil.toList("text/html"));
        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("X-Apple-Client-Application",ListUtil.toList("Software"));
        headers.put("X-Apple-Store-Front",ListUtil.toList(MapUtil.getStr(paras,"storeFront")));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("User-Agent",ListUtil.toList("MacAppStore/2.0 (Macintosh; OS X 12.10) AppleWebKit/600.1.3.41"));
        String cookies = MapUtil.getStr(paras,"cookies","");
        String appStoreOverCheckUrl = MapUtil.getStr(paras,"appStoreOverCheckUrl","");
        //获取支付方式
        HttpResponse response = HttpUtil.createRequest(Method.GET,appStoreOverCheckUrl)
                .header(headers)
                .cookie(cookies)
                .execute();
        System.out.println(response.getStatus());
        System.out.println(response.body());
        return result;
    }
    /**
    　* 获取礼品卡信息
      * @param
    　* @return cn.hutool.http.HttpResponse
    　* @throws
    　* @author DeZh
    　* @date 2024/1/23 14:29
    */
    public static HttpResponse getCodeInfoSrv(LoginInfo loginInfo, String giftCardCode){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Content-Type", ListUtil.toList("text/html"));
        headers.put("Host", ListUtil.toList("p"+loginInfo.getItspod()+"-buy.itunes.apple.com"));
        headers.put("X-Apple-Client-Application",ListUtil.toList("Software"));
        headers.put("X-Apple-Store-Front",ListUtil.toList(loginInfo.getStoreFront()));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("User-Agent",ListUtil.toList("MacAppStore/2.0 (Macintosh; OS X 12.10) AppleWebKit/600.1.3.41"));
        HttpResponse httpResponse = HttpUtil.createRequest(Method.GET,"https://p"+loginInfo.getItspod()+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/getCodeInfoSrv?code="+giftCardCode)
                .header(headers)
                .cookie(loginInfo.getCookie())
                .execute();
        return httpResponse;
    }

    public static Map<String,Object> editAccountFieldsSrv(Map<String, Object> paras) {
        String accountUrl = "https://p"+ paras.get("itspod") +"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/?context=changeCountry";
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList("MacAppStore/2.0 (Macintosh; OS X 12.10) AppleWebKit/600.1.3.41"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("Origin",ListUtil.toList("https://finance-app.itunes.apple.com"));
        headers.put("Referer",ListUtil.toList("https://finance-app.itunes.apple.com/"));
//        headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
//        headers.put("X-Apple-Store-Front",ListUtil.toList(paras.get("storeFront").toString()));
//        headers.put("X-Token",ListUtil.toList(paras.get("passwordToken").toString()));
        headers.put("Accept-Encoding",ListUtil.toList("gzip"));

        String cookies = MapUtil.getStr(paras,"cookies","");
        try {
            HttpResponse res = HttpUtil.createGet(accountUrl)
                    .header(headers)
                    .cookie(cookies)
                    .execute();
            System.out.println(res.body());




        } catch (Exception e) {
            e.printStackTrace();
        }
        return paras;
    }



    /**
     　* 修改账号国家
     * @param
     * @param paras
    　* @return java.util.Map<java.lang.String,java.lang.Object>
    　* @throws
    　* @author DeZh
    　* @date 2023/12/24 21:03
     */
    public  static Map<String,Object> editBillingInfo(Map<String,Object> paras){
        boolean hasInspectionFlag=MapUtil.getBool(paras,"hasInspectionFlag");
        Map<String,Object> result=new HashMap<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("X-Apple-Store-Front", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("X-Apple-Client-Application",ListUtil.toList("Software"));
        if(hasInspectionFlag){
            headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
            headers.put("X-Token",ListUtil.toList(paras.get("passwordToken").toString()));
        }

        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded; charset=UTF-8"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.9,en;q=0"));
        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/account/storefront/edit/billing-info"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        String guid=MapUtil.getStr(paras,"guid","");
        String body=MapUtil.getStr(paras,"addressInfo");
        String url="https://p"+paras.get("itspod")+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/editBillingInfoSrv?guid="+guid;
        HttpResponse response = HttpUtil.createRequest(Method.POST,url)
                .header(headers)
                .cookie(MapUtil.getStr(paras,"cookies",""))
                .body(body)
                .execute();
        if(response.getStatus()==200){
            JSON bodyJson= JSONUtil.parse(response.body());
            int status=bodyJson.getByPath("status",int.class);
            if(status==0){
                result.put("code",Constant.SUCCESS);
                result.put("message","修改成功");
            }else{
                StringBuffer stringBuffer=new StringBuffer();
                String validationResults= bodyJson.getByPath("result.validationResults",String.class);
                if(!StringUtils.isEmpty(validationResults)){
                    System.out.println(validationResults);
                    JSONArray jsonArray=JSONUtil.parseArray(validationResults);
                    for(Object jsonObject:jsonArray){
                        JSON json=JSONUtil.parse(jsonObject);
                        String validationRuleName=json.getByPath("errorString",String.class);
                        if(!StringUtils.isEmpty(validationRuleName)){
                            switch (validationRuleName){
                                case "INVALID_PHONE_NUMBER":
                                    stringBuffer.append("手机号码不正确，请更新并重试。");
                                    stringBuffer.append("\n");
                                    break;
                                default:
                                    String errorString= json.getByPath("errorString",String.class);
                                    stringBuffer.append(errorString);
                                    stringBuffer.append("\n");
                            }
                        }else if(!StringUtils.isEmpty(json.getByPath("validationRuleName",String.class))){
                            String field=json.getByPath("field",String.class);
                            switch (field){
                                case "Account.XCardBalance":
                                    stringBuffer.append("你还有店面点数余额；必须先用完该点数，才能更改店面。");
                                    stringBuffer.append("\n");
                                    break;
                                default:
                                    String errorString= json.getByPath("localizedMessage",String.class);
                                    stringBuffer.append(errorString);
                                    stringBuffer.append("\n");
                            }
                        }
                    }
                }else if(!StringUtils.isEmpty(bodyJson.getByPath("errorMessageKey",String.class))){
                    String userPresentableErrorMessage= bodyJson.getByPath("userPresentableErrorMessage",String.class);
                    stringBuffer.append(userPresentableErrorMessage);
                }
                result.put("code","-1");
                result.put("message",stringBuffer.toString());
            }
        }else{
            result.put("code","-1");
            result.put("message","修改失败");
        }
        return result;
    }

    public static HttpResponse authenticate(String account,String pwd,String authCode,String guid,String authUrl){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", ListUtil.toList(ContentType.FORM_URLENCODED.getValue()));
        headers.put("User-Agent", ListUtil.toList("Configurator/2.15 (Macintosh; OS X 11.0.0; 16G29) AppleWebKit/2603.3.8"));
        String authBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">" +
                "<plist version=\"1.0\">" +
                "    <dict>" +
                "        <key>appleId</key>" +
                "        <string>%s</string>" +
                "        <key>attempt</key>" +
                "        <string>4</string>" +
                "        <key>createSession</key>" +
                "        <string>true</string>" +
                "        <key>guid</key>" +
                "        <string>%s</string>" +
                "        <key>password</key>" +
                "        <string>%s%s</string>" +
                "        <key>rmp</key>" +
                "        <string>0</string>" +
                "        <key>why</key>" +
                "        <string>signIn</string>" +
                "    </dict>" +
                "</plist>";
        authBody = String.format(authBody,account,guid,pwd,authCode);
        HttpResponse authRsp = HttpUtil.createPost(authUrl)
                .header(headers)
                .body(authBody, ContentType.FORM_URLENCODED.getValue())
                .execute();
        return authRsp;
    }

    /**
     * 搜索应用
     * @param country 商城地区 StoreFontsUtils.getCountryCodeFromStoreFront()
     * @param term  搜索关键字
     * @param limit 条数
     */
    public static HttpResponse appstoreSearch(String country,String term,int limit){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList("Configurator/2.15 (Macintosh; OS X 11.0.0; 16G29) AppleWebKit/2603.3.8"));
        String params = "entity=software,iPadSoftware&media=software&country="+country+"&term="+term+"&limit="+limit;
        String searchUrl = "https://itunes.apple.com/search?" + params;
        HttpResponse searchRsp = HttpUtil.createGet(searchUrl)
                .header(headers)
                .execute();
        return searchRsp;
    }

    /**
     * 购买
     */
    public static HttpResponse purchase(AppstoreDownloadVo appstoreDownloadVo, String trackId, String url) {
        String itspod = appstoreDownloadVo.getItspod();
        String dsPersonId = appstoreDownloadVo.getDsPersonId();
        String storeFront = appstoreDownloadVo.getStoreFront();
        String passwordToken = appstoreDownloadVo.getPasswordToken();

        url = StrUtil.isEmpty(url) ? "https://p"+ itspod +"-buy.itunes.apple.com/WebObjects/MZBuy.woa/wa/buyProduct" : url;

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList("Configurator/2.15 (Macintosh; OS X 11.0.0; 16G29) AppleWebKit/2603.3.8"));
        headers.put("Content-Type", ListUtil.toList("application/x-apple-plist"));

        headers.put("iCloud-DSID", ListUtil.toList(dsPersonId));
        headers.put("X-Dsid",ListUtil.toList(dsPersonId));
        headers.put("X-Apple-Store-Front",ListUtil.toList(storeFront));
        headers.put("X-Token",ListUtil.toList(passwordToken));

        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "    <dict>\n" +
                "        <key>appExtVrsId</key>\n" +
                "        <string>0</string>\n" +
                "        <key>buyWithoutAuthorization</key>\n" +
                "        <string>true</string>\n" +
                "        <key>guid</key>\n" +
                "        <string>%s</string>\n" +
                "        <key>hasAskedToFulfillPreorder</key>\n" +
                "        <string>true</string>\n" +
                "        <key>hasDoneAgeCheck</key>\n" +
                "        <string>true</string>\n" +
                "        <key>needDiv</key>\n" +
                "        <string>0</string>\n" +
                "        <key>origPage</key>\n" +
                "        <string>Software-%s</string>\n" +
                "        <key>origPageLocation</key>\n" +
                "        <string>Buy</string>\n" +
                "        <key>price</key>\n" +
                "        <string>0</string>\n" +
                "        <key>pricingParameters</key>\n" +
                "        <string>STDQ</string>\n" +
                "        <key>productType</key>\n" +
                "        <string>C</string>\n" +
                "        <key>salableAdamId</key>\n" +
                "        <integer>%s</integer>\n" +
                "    </dict>\n" +
                "</plist>";
        body = String.format(body,appstoreDownloadVo.getGuid(),trackId,trackId);
        HttpResponse purchaseRsp = HttpUtil.createPost(url)
                .header(headers)
                .cookie(appstoreDownloadVo.getCookie())
                .body(body)
                .execute();

        // 重定向
        if(purchaseRsp.getStatus() == 307 || purchaseRsp.getStatus() ==302){
            String location = purchaseRsp.header("Location");
            purchaseRsp = purchase(appstoreDownloadVo,trackId,location);
        }

        return purchaseRsp;
    }

    /**
     * 获取appstore下载地址
     */
    public static HttpResponse appstoreDownloadUrl(AppstoreDownloadVo appstoreDownloadVo,String trackId,String downloadUrl) {
        if (StrUtil.isEmpty(downloadUrl)){
            downloadUrl = "https://p34-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/volumeStoreDownloadProduct?guid="+appstoreDownloadVo.getGuid();
        }
        String dsPersonId = appstoreDownloadVo.getDsPersonId();
        String storeFront = appstoreDownloadVo.getStoreFront();
        String passwordToken = appstoreDownloadVo.getPasswordToken();

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList("Configurator/2.15 (Macintosh; OS X 11.0.0; 16G29) AppleWebKit/2603.3.8"));
        headers.put("Content-Type", ListUtil.toList("application/x-apple-plist"));

        headers.put("iCloud-DSID", ListUtil.toList(dsPersonId));
        headers.put("X-Dsid",ListUtil.toList(dsPersonId));
        headers.put("X-Apple-Store-Front",ListUtil.toList(storeFront));
        headers.put("X-Token",ListUtil.toList(passwordToken));

        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "    <dict>\n" +
                "        <key>creditDisplay</key>\n" +
                "        <string/>\n" +
                "        <key>guid</key>\n" +
                "        <string>%s</string>\n" +
                "        <key>salableAdamId</key>\n" +
                "        <integer>%s</integer>\n" +
                "    </dict>\n" +
                "</plist>";
        body = String.format(body,appstoreDownloadVo.getGuid(),trackId);
        HttpResponse downloadRsp = HttpUtil.createPost(downloadUrl)
                .header(headers)
                .cookie(appstoreDownloadVo.getCookie())
                .body(body)
                .execute();

        if(downloadRsp.getStatus() == 307 || downloadRsp.getStatus() ==302){
            return appstoreDownloadUrl(appstoreDownloadVo,trackId,downloadRsp.header("Location"));
        }
        return downloadRsp;
    }

    /**
     * 礼品卡兑换
     */
    public static HttpResponse redeem(GiftCardRedeem giftCardRedeem,String redeemUrl){
        String itspod        = giftCardRedeem.getItspod();
        String storeFront    = giftCardRedeem.getStoreFront();
        String dsPersonId    = giftCardRedeem.getDsPersonId();
        String passwordToken = giftCardRedeem.getPasswordToken();
        String guid          = giftCardRedeem.getGuid();
        String cardCode      = giftCardRedeem.getGiftCardCode();

        if (StrUtil.isEmpty(redeemUrl)){
            redeemUrl = "https://p"+ itspod +"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/redeemCodeSrv";
        }
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList("MacAppStore/2.0 (Macintosh; OS X 12.10) AppleWebKit/600.1.3.41"));
        headers.put("Content-Type",ListUtil.toList("application/x-apple-plist"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("X-Dsid",ListUtil.toList(dsPersonId));
        headers.put("X-Apple-Store-Front",ListUtil.toList(storeFront));
        headers.put("X-Token",ListUtil.toList(passwordToken));
        headers.put("Connection",ListUtil.toList("close"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip"));


        String redeemBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<plist version=\"1.0\">\n" +
                "\t<dict>\n" +
                "\t\t<key>attemptCount</key>\n" +
                "\t\t<string>1</string>\n" +
                "\t\t<key>cameraReCOGnizedCode</key>\n" +
                "\t\t<string>false</string>\n" +
                "\t\t<key>cl</key>\n" +
                "\t\t<string>iTunes</string>\n" +
                "\t\t<key>code</key>\n" +
                "\t\t<string>"+ cardCode +"</string>\n" +
                "\t\t<key>dsPersonId</key>\n" +
                "\t\t<string>"+ dsPersonId +"</string>\n" +
                "\t\t<key>guid</key>\n" +
                "\t\t<string>"+ guid +"</string>\n" +
                "\t\t<key>has4GBLimit</key>\n" +
                "\t\t<string>false</string>\n" +
                "\t\t<key>kbsync</key>\n" +
                "\t\t<data></data>\n" +
                "\t\t<key>pg</key>\n" +
                "\t\t<string>Music</string>\n" +
                "\t\t<key>response-content-type</key>\n" +
                "\t\t<string>application/json</string>\n" +
                "\t</dict>\n" +
                "</plist>";

            HttpResponse redeemRsp = HttpUtil.createPost(redeemUrl)
                    .header(headers)
                    .cookie(giftCardRedeem.getCookie())
                    .body(redeemBody)
                    .execute();
            return redeemRsp;
    }


    public static void main(String[] args) throws Exception {
//        accountPurchasesCount(null);
        //getPurchases(null);
//        getPaymentInfos(null);
        // addOrEditBillingInfoSrv(null);
//        Faker faker = new Faker(Locale.CHINA);
//
//        Address address = faker.address();
//        System.out.println(address.streetAddress());
//        System.out.println(address.streetName());
//        System.out.println(address.secondaryAddress());
//        System.out.println(address.streetAddressNumber());
//        System.out.println(address.buildingNumber());
//        System.out.println(address.citySuffix());
//        System.out.println(address.stateAbbr());
//        String ZipCode=address.zipCode();
//        System.out.println(address.countyByZipCode(ZipCode));
//        System.out.println(faker.phoneNumber().subscriberNumber());


//        String jsonString = ResourceUtil.readUtf8Str("json/global-mobile-phone-regular.json");
//////
//        JSONArray jsonArray= JSONUtil.parseArray(JSONUtil.parseObj(jsonString).getStr("data"));
//        for (Object o:jsonArray){
//            JSONObject jsonObject= (JSONObject) o;
//            System.out.println(jsonObject.getStr("locale"));
//        }
//        Generex generex = new Generex("1[35789]\\d{9}");
//            System.out.println(generex.random());

        //downloadDemo();
//        subscriptionDemo();
    }

    private static void subscriptionDemo(){
        Account account = new Account();
//        account.setAccount("djli0506@163.com");
//        account.setPwd("!!B0527s0207!!");
        account.setAccount("qewqeq@2980.com");
        account.setPwd("dPFb6cSD41");

        String guid = DataUtil.getGuidByAppleId(account.getAccount());
        HttpResponse authRsp = null;
        String storeFront = "";

        // 鉴权
        if (authRsp != null && authRsp.getStatus() == 200){
            JSONObject authBody = PListUtil.parse(authRsp.body());
            String firstName = authBody.getByPath("accountInfo.address.firstName",String.class);
            String lastName  = authBody.getByPath("accountInfo.address.lastName",String.class);
            String creditDisplay  = authBody.getByPath("creditDisplay",String.class);
            Boolean isDisabledAccount  = authBody.getByPath("accountFlags.isDisabledAccount",Boolean.class);

            Console.log("Account firstName: {}, lastName:{}, creditDisplay:{}, isDisabledAccount:{}",firstName,lastName,creditDisplay,isDisabledAccount);
            storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);

            String itspod = authRsp.header(Constant.ITSPOD);
            String dsPersonId = authBody.getStr("dsPersonId","");
            String passwordToken = authBody.getStr("passwordToken","");

            String url = "https://p"+itspod+"-buy.itunes.apple.com/commerce/account/subscriptions?prevpage=accountsettings&version=2.0";
            HashMap<String, List<String>> headers = new HashMap<>();
            headers.put("Origin",List.of("https://finance-app.itunes.apple.com"));
            headers.put("Referer",List.of("https://finance-app.itunes.apple.com"));
            //headers.put("User-Agent", ListUtil.toList("Configurator/2.15 (Macintosh; OS X 11.0.0; 16G29) AppleWebKit/2603.3.8"));
            headers.put("User-Agent", ListUtil.toList("iTunes/12.13 (Windows; Microsoft Windows 10 x64 (Build 19045); x64) AppleWebKit/7613.2007.1014.14 (dt:2)"));
            headers.put("Accept-Encoding",List.of("gzip, deflate"));
            headers.put("Connection",List.of("keep-alive"));
            headers.put("X-Apple-Tz",ListUtil.toList("28800"));
            headers.put("Accept",List.of("*/*"));
            headers.put("Accept-Language",List.of("zh-cn"));

            headers.put("X-Dsid",ListUtil.toList(dsPersonId));
            headers.put("X-Apple-Store-Front",ListUtil.toList(storeFront));
            headers.put("X-Token",ListUtil.toList(passwordToken));
            headers.put("Cookie",List.of(getCookie(authRsp)));

            HttpResponse subscriptionsRsp = HttpUtil.createGet(url)
                    .header(headers)
//                    .cookie(getCookie(authRsp))
                    .execute();
            System.err.println(subscriptionsRsp);
        }
    }

    private static String getCookie(HttpResponse rsp) {
        StringBuilder cookieBuilder = new StringBuilder();
        List<String> res1Cookies = rsp.headers().get("Set-Cookie");
        List<String> res2Cookies = rsp.headers().get("set-cookie");

        if (res1Cookies != null) {
            for (String item : res1Cookies) {
                cookieBuilder.append(";").append(item);
            }
        }
        if (res2Cookies != null) {
            for (String item : res2Cookies) {
                cookieBuilder.append(";").append(item);
            }
        }
        String cookies = "";
        if(cookieBuilder.toString().length() > 0){
            cookies = cookieBuilder.toString().substring(1);
        }
        return cookies;
    }

}

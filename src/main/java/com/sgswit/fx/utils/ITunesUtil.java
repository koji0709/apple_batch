package com.sgswit.fx.utils;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.iTunes.vo.AppstoreDownloadVo;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.utils.proxy.ProxyUtil;
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
    /**
     　* 统计购买记录
     * @param
    　* @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
    　* @throws
    　* @author DeZh
    　* @date 2023/10/19 10:09
     */
    public  static List<Map<String,String>> accountPurchasesCount(Map<String,Object> paras){
        List<Map<String,String>> result=new ArrayList<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));
        headers.put("Origin", ListUtil.toList("https://finance-app.itunes.apple.com"));

        headers.put("User-Agent",ListUtil.toList(Constant.MACAPPSTORE20_USER_AGENT));
        headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("X-Token",ListUtil.toList(paras.get("passwordToken").toString()));
        String cookies = MapUtil.getStr(paras,"cookies","");
        String url="https://p"+paras.get("itspod")+"-buy.itunes.apple.com/commerce/account/purchases/count?isDeepLink=false&isJsonApiFormat=true&page=1";
        HttpResponse step4Res = ProxyUtil.execute(HttpUtil.createRequest(Method.GET,url)
                        .header(headers)
                        .cookie(cookies));
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

        HttpRequest request=HttpUtil.createRequest(Method.GET,"https://p"+paras.get("itspod")+"-buy.itunes.apple.com/account/stackable/paymentInfos?managePayments=true")
                .header(headers)
                .cookie(cookies);

        HttpResponse httpResponse = ProxyUtil.execute(request);
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
                    HttpResponse delHttpResponse = ProxyUtil.execute(HttpUtil.createRequest(Method.POST,url)
                                    .header(headers)
                                    .cookie(cookies));
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
        HttpRequest httpRequest=HttpUtil.createRequest(Method.POST,"https://p"+paras.get("itspod")+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/addOrEditBillingInfoSrv?guid="+guid)
                .header(headers)
                .cookie(MapUtil.getStr(paras,"cookies",""))
                .body(body);
        HttpResponse response = ProxyUtil.execute(httpRequest);
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
        headers.put("User-Agent",ListUtil.toList(Constant.MACAPPSTORE20_USER_AGENT));
        String cookies = MapUtil.getStr(paras,"cookies","");
        //获取支付方式
        String url="https://p"+paras.get("itspod")+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/redeemLandingPage?cc=cn";
        HttpResponse response = ProxyUtil.execute(HttpUtil.createRequest(Method.GET,url)
                        .header(headers)
                        .cookie(cookies));
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
        headers.put("User-Agent",ListUtil.toList(Constant.MACAPPSTORE20_USER_AGENT));
        String cookies = MapUtil.getStr(paras,"cookies","");
        //获取支付方式
        Map<String, String> source=new HashMap<>();
        source.put("response-content-type", "application/json");
        source.put("name", MapUtil.getStr(paras,"name"));
        source.put("phone", MapUtil.getStr(paras,"phone"));
        source.put("nationalId", MapUtil.getStr(paras,"nationalId"));
        String body=MapUtil.join(source,"&","=",false);
        String url="https://p"+paras.get("itspod")+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/redeemValidateId?"+body;
        HttpResponse response = ProxyUtil.execute(HttpUtil.createRequest(Method.POST,url)
                        .header(headers)
                        .cookie(cookies));
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
        headers.put("User-Agent",ListUtil.toList(Constant.MACAPPSTORE20_USER_AGENT));
        String cookies = MapUtil.getStr(paras,"cookies","");
        String appStoreOverCheckUrl = MapUtil.getStr(paras,"appStoreOverCheckUrl","");
        //获取支付方式
        HttpResponse response = ProxyUtil.execute(HttpUtil.createRequest(Method.GET,appStoreOverCheckUrl)
                        .header(headers)
                        .cookie(cookies));
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
        headers.put("User-Agent",ListUtil.toList(Constant.MACAPPSTORE20_USER_AGENT));
        HttpRequest httpRequest=HttpUtil.createRequest(Method.GET,"https://p"+loginInfo.getItspod()+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/getCodeInfoSrv?code="+giftCardCode)
                .header(headers)
                .cookie(loginInfo.getCookie());
        HttpResponse httpResponse = ProxyUtil.execute(httpRequest);
        return httpResponse;
    }

    public static Map<String,Object> editAccountFieldsSrv(Map<String, Object> paras) {
        String accountUrl = "https://p"+ paras.get("itspod") +"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/?context=changeCountry";
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.MACAPPSTORE20_USER_AGENT));
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
            HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(accountUrl)
                            .header(headers)
                            .cookie(cookies));
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
        headers.put("X-Apple-Store-Front",ListUtil.toList(MapUtil.getStr(paras,"storeFront")));
//        headers.put("X-Apple-Store-Front", ListUtil.toList("143465-19,17"));
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
        HttpRequest httpRequest=HttpUtil.createRequest(Method.POST,url)
                .header(headers)
                .cookie(MapUtil.getStr(paras,"cookies",""))
                .body(body);
        HttpResponse response = ProxyUtil.execute(httpRequest);
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
        headers.put("X-Apple-Store-Front", ListUtil.toList("143465-19,17"));

        headers.put("User-Agent", ListUtil.toList(Constant.CONFIGURATOR_USER_AGENT));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9,en;q=0"));
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
        HttpResponse authRsp = ProxyUtil.execute(HttpUtil.createPost(authUrl)
                        .header(headers)
                        .body(authBody, ContentType.FORM_URLENCODED.getValue()));
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
        headers.put("User-Agent", ListUtil.toList(Constant.CONFIGURATOR_USER_AGENT));
        String params = "entity=software,iPadSoftware&media=software&country="+country+"&term="+term+"&limit="+limit;
        String searchUrl = "https://itunes.apple.com/search?" + params;
        HttpResponse searchRsp = ProxyUtil.execute(HttpUtil.createGet(searchUrl)
                        .header(headers));
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
        headers.put("User-Agent", ListUtil.toList(Constant.CONFIGURATOR_USER_AGENT));
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
        HttpResponse purchaseRsp = ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers)
                        .cookie(appstoreDownloadVo.getCookie())
                        .body(body));

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
        headers.put("User-Agent", ListUtil.toList(Constant.CONFIGURATOR_USER_AGENT));
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
        HttpResponse downloadRsp = ProxyUtil.execute(HttpUtil.createPost(downloadUrl)
                        .header(headers)
                        .cookie(appstoreDownloadVo.getCookie())
                        .body(body));

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
        headers.put("User-Agent",ListUtil.toList(Constant.MACAPPSTORE20_USER_AGENT));
        headers.put("Content-Type",ListUtil.toList("application/x-apple-plist; Charset=UTF-8"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("X-Dsid",ListUtil.toList(dsPersonId));
        headers.put("X-Apple-Store-Front",ListUtil.toList(storeFront));
        headers.put("X-Token",ListUtil.toList(passwordToken));
        headers.put("Connection",ListUtil.toList("close"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9,en;q=0"));

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

            HttpResponse redeemRsp = ProxyUtil.execute(HttpUtil.createPost(redeemUrl)
                            .header(headers)
                            .cookie(giftCardRedeem.getCookie())
                            .body(redeemBody),false);
            return redeemRsp;
    }
    /**
    　* 校验iTunes登录结果
      * @param
     * @param res
    　* @return java.util.Map<java.lang.String,java.lang.Object>
    　* @throws
    　* @author DeZh
    　* @date 2024/7/18 11:36
    */
    public static Map<String, Object> checkLoginRes(String res){
        Map<String,Object> result=new HashMap<>();
        JSONObject rspJSON = PListUtil.parse(res);
        boolean m_allowed =rspJSON.getByPath("m-allowed",boolean.class);
        //登录失败
        if(!m_allowed){
            String customerMessage=rspJSON.getStr("customerMessage");
            String failureType=rspJSON.getStr("failureType");
            if(StringUtils.isEmpty(failureType)  && Constant.CustomerMessageBadLogin.equals(customerMessage)){
                result.put("code",Constant.TWO_FACTOR_AUTHENTICATION);
                result.put("msg","Apple ID或密码错误。或需要输入双重验证码！");
            }else{
                String dialogId=  rspJSON.getByPath("metrics.dialogId",String.class);
                if(!StrUtil.isEmpty(dialogId)){
                    if(dialogId.equalsIgnoreCase(Constant.MZFinanceDisabledAndFraudLocked)){
                        result.put("code","-1");
                        result.put("msg","帐户存在欺诈行为，已被【双禁】！");
                    }else  if(dialogId.equalsIgnoreCase(Constant.MZFinanceAccountDisabled)){
                        result.put("code","-1");
                        result.put("msg","出于安全原因，你的账户已被锁定。");
                    }else  if(dialogId.equalsIgnoreCase(Constant.MZFinanceAccountConversion)){
                        result.put("code",Constant.CustomerMessageNotYetUsediTunesStoreCode);
                        result.put("msg","此 Apple ID 尚未用于 App Store。");
                    }
                }else{
                    result.put("code","-1");
                    result.put("msg",customerMessage);
                }
            }
        }else{
            //登录成功
            result.put("code",Constant.SUCCESS);
            result.put("msg","登录成功！");
            return result;

        }
        return result;
    }

}

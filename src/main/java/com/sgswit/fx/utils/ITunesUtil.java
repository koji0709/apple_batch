package com.sgswit.fx.utils;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.http.useragent.OS;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.XMLPropertyListParser;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.Account;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.ParseException;
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

        String cookie="wosid=LBsKRssFyD0ZND4BKNqJeM;woinst=-1;ns-mzf-inst=240-29-443-112-216-9041-3300097-30-mr30;mzf_in=3300097;mzf_dr=0;hsaccnt=1;session-store-id=DE803D26F256B83EE2FF0995D148C221;X-Dsid=8135448658;mz_at0-8135448658=AwQAAAEBAAH70AAAAABlJOhxwbF3d2O6TPHcEKYKAFvwothLUEM=;ampsc=But8SXC+zW5BZC2G3dKqYW5AwB6BSZpcKKRWoQi1xbQ=;mz_at_ssl-8135448658=AwUAAAEBAAH70AAAAABlJOhxNjR8LaUW1MlaenhYHwMSnqs/u24=;mz_at_mau-8135448658=AwIAAAEBAAH70AAAAABlJOhxSvg154dYPDoZtKkVRQCIWDzD+Js=;pldfltcid=33c1e4e8ac6640f6961f682af925fe1a030;wosid-lite=ZcLHGT6BVCNQUjSxI3OfYM;itspod=30;";


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
        String cookie="amp=wGmqWc+tU5BPYIxtZTS67zmX7VrVrSK6kLs9DN0xJcLT0EGAfagTSF2by2JACpWA7IZUM75ULaCeNad0jCUC0DcoHzfVDRsOCoXwcLQaywM=; mt-tkn-8135448658=AtZhe8l+IrVrzE0Naxsi8hLyXmMyrK1YeiRwrxGTnqJNskBCWiKHYjr5aWpsMZxcXe4J2YGfaGBax9AOYXijgdfcP0WylhotJ0RI2NZbr2PRx7CmxrXbBMq5iZupygq8T2U9ulOPcVxCXBrdTQNQlcH5HfY3WJv3mrW/7B464SI935H2G2D4ep6nrvQKckGJmNl3ycA=; mz_mt0-8135448658=AolMJGAKMP7xwnuOPTRZVKoxnSgmdfuNWBuyhG9MsUMkn//PB6cAvSmDl52ZUxImL6886q9Lg/uJKTi1/4o+PsTsUd4aCLJ7hMXBQLcweDOP0/mzgOO3oqDU1N9ym2VtsSPp88TmXOLR0Fsj3JvPnQ8yafFGM9lxfjWmkwWoeiNK6/RNn2vUelMBE4YFO6sLwF5sQQo=; ampsc=ukfVwNIDoZHQtu2N9c1CJ7CKqFmT9T7J/1arwOfEnzY=; itspod=30; mz_at0-8135448658=AwQAAAECAAHZ1AAAAABlbuYK6+eAeRvWUZmm5fGYvWdgNiCjsMg=; mz_at_ssl-8135448658=AwUAAAECAAHZ1AAAAABlbuYKzGpJ/FCnkSdpeD1H2uN8r/uBvn8=; pldfltcid=33c1e4e8ac6640f6961f682af925fe1a030; wosid-lite=GtN5UQnYgi8Fj9XbbKwJg0; X-Dsid=8135448658; xp_ci=";

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
                result.put("code","1");
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
        boolean hasInspectionFlag=MapUtils.getBool(paras,"hasInspectionFlag");
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
        headers.put("Accept-Language", ListUtil.toList("h-CN,zh;q=0.9,en;q=0"));
        headers.put("Host", ListUtil.toList("p"+paras.get("itspod")+"-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/account/storefront/edit/billing-info"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        String guid=MapUtil.getStr(paras,"guid","");
        String body=MapUtils.getStr(paras,"addressInfo");
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
    public static HttpResponse purchase(HttpResponse authRsp,String guid,String trackId,String url) {
        JSONObject json = PListUtil.parse(authRsp.body());
        String itspod = authRsp.header(Constant.ITSPOD);
        String dsPersonId = json.getStr("dsPersonId","");
        String storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
        String passwordToken = json.getStr("passwordToken","");

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
        body = String.format(body,guid,trackId,trackId);
        HttpResponse purchaseRsp = HttpUtil.createPost(url)
                .header(headers)
                .body(body)
                .execute();

        // 重定向
        if(purchaseRsp.getStatus() == 307 || purchaseRsp.getStatus() ==302){
            String location = purchaseRsp.header("Location");
            purchaseRsp = purchase(authRsp,guid,trackId,location);
        }

        return purchaseRsp;
    }

    /**
     * 获取appstore下载地址
     */
    public static HttpResponse appstoreDownloadUrl(HttpResponse authRsp,String guid,String trackId,String downloadUrl) {
        if (StrUtil.isEmpty(downloadUrl)){
            downloadUrl = "https://p34-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/volumeStoreDownloadProduct?guid="+guid;
        }

        JSONObject json = PListUtil.parse(authRsp.body());

        String dsPersonId = json.getStr("dsPersonId","");
        String storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
        String passwordToken = json.getStr("passwordToken","");

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
        body = String.format(body,guid,trackId);
        HttpResponse downloadRsp = HttpUtil.createPost(downloadUrl)
                .header(headers)
                .cookie(authRsp.getCookies())
                .body(body)
                .execute();

        if(downloadRsp.getStatus() == 307 || downloadRsp.getStatus() ==302){
            return appstoreDownloadUrl(authRsp,guid,trackId,downloadRsp.header("Location"));
        }
        return downloadRsp;
    }

    /**
     * 礼品卡兑换
     */
    public static HttpResponse redeem(HttpResponse authRsp,String guid,String cardCode){
        JSONObject authBody = PListUtil.parse(authRsp.body());
        String itspod = authRsp.header(Constant.ITSPOD);
        String storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
        String dsPersonId = authBody.getStr("dsPersonId","");
//        String dsPersonId = "1234";
        String passwordToken = authBody.getStr("passwordToken","");

        String redeemUrl = "https://p"+ itspod +"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/redeemCodeSrv";
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
                    .cookie(getCookie(authRsp))
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
        redeemDemo();
    }


    private static void downloadDemo(){
        Account account = new Account();
//        account.setAccount("djli0506@163.com");
//        account.setPwd("!!B0527s0207!!");
        account.setAccount("qewqeq@2980.com");
        account.setPwd("dPFb6cSD41");

        String guid = DataUtil.getGuidByAppleId(account.getAccount());
        //HttpResponse authRsp = ITunesUtil.authenticate(account,"",guid);
        HttpResponse authRsp = null;
        String storeFront = "";

        // 鉴权
        if (authRsp != null && authRsp.getStatus() == 200){
            JSONObject rspJSON = PListUtil.parse(authRsp.body());
            String firstName = rspJSON.getByPath("accountInfo.address.firstName",String.class);
            String lastName  = rspJSON.getByPath("accountInfo.address.lastName",String.class);
            String creditDisplay  = rspJSON.getByPath("creditDisplay",String.class);
            Boolean isDisabledAccount  = rspJSON.getByPath("accountFlags.isDisabledAccount",Boolean.class);

            Console.log("Account firstName: {}, lastName:{}, creditDisplay:{}, isDisabledAccount:{}",firstName,lastName,creditDisplay,isDisabledAccount);
            storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
        }

        // 搜索应用
        if (!StrUtil.isEmpty(storeFront)){
            String countryCode = StoreFontsUtils.getCountryCodeFromStoreFront(storeFront);
            Console.log("请输入关键字: ");
            String term = Console.input();
            HttpResponse appstoreSearchRsp = ITunesUtil.appstoreSearch(countryCode, term, 2);
            if (appstoreSearchRsp.getStatus() == 200){
                String appstoreSearchBody = appstoreSearchRsp.body();
                JSONObject entries = JSONUtil.parseObj(appstoreSearchBody);
                JSONArray results = entries.getJSONArray("results");
                if (!CollUtil.isEmpty(results)){
                    for (Object result : results) {
                        JSONObject track = (JSONObject) result;
//                        Long trackId = track.getLong("trackId");
                        Long trackId = 1232780281L;
                        String trackName = track.getStr("trackName");
                        String artworkUrl100 = track.getStr("artworkUrl100");
                        Double price = track.getDouble("price");
                        Console.log("APP [{}] trackId:{}, price:{}, icon:{}",trackName,trackId,price,artworkUrl100);

                        // 购买
                        if (price > 0){
                            Console.log("暂只支持免费应用！[{}] 价格:{}",trackName,price);
                            continue;
                        }

                        HttpResponse purchaseRsp = ITunesUtil.purchase(authRsp, guid, trackId.toString(),"");
                        String purchaseBody = purchaseRsp.body();

                        if (!StrUtil.isEmpty(purchaseBody) && JSONUtil.isTypeJSON(purchaseBody)){
                            JSONObject purchaseJSON = JSONUtil.parseObj(purchaseBody);
                            String purchaseJdt = purchaseJSON.getStr("jingleDocType","");
                            String purchaseStatus   = purchaseJSON.getStr("status","");
                            if(!"purchaseSuccess".equals(purchaseJdt) || !"0".equals(purchaseStatus)){
                                String failureType = purchaseJSON.getStr("failureType");
                                String customerMessage = purchaseJSON.getStr("customerMessage");
                                Console.log("[{}]购买失败！ failureType:{}, customerMessage:{}",trackName,failureType,customerMessage);
                                continue;
                            }
                        }

                        Console.log("[{}] 购买成功",trackName);

                        HttpResponse appstoreDownloadUrlRsp = ITunesUtil.appstoreDownloadUrl(authRsp, guid, trackId.toString(), "");
                        JSONObject appstoreDownloadUrlBody = PListUtil.parse(appstoreDownloadUrlRsp.body());

                        String appstoreDownloadUrlJdt = appstoreDownloadUrlBody.getStr("jingleDocType","");
                        String appstoreDownloadUrlStatus   = appstoreDownloadUrlBody.getStr("status","");
                        if(!"purchaseSuccess".equals(appstoreDownloadUrlJdt) || !"0".equals(appstoreDownloadUrlStatus)){
                            String failureType = appstoreDownloadUrlBody.getStr("failureType");
                            String customerMessage = appstoreDownloadUrlBody.getStr("customerMessage");
                            Console.log("[{}] 获取下载链接失败！ failureType:{}, customerMessage:{}",trackName,failureType,customerMessage);
                            continue;
                        }

                        JSONArray songList = appstoreDownloadUrlBody.getJSONArray("songList");
                        if (songList.isEmpty()){
                            Console.log("songList is empty");
                            continue;
                        }

                        JSONObject song = (JSONObject)songList.get(0);
                        String url = song.getStr("URL");
                        JSONObject metadata = song.getJSONObject("metadata");

                        String filePath = "/Users/koji/workspace/tmp/";
                        String fileName = String.format("%s-%s-%s.ipa"
                                ,metadata.getStr("softwareVersionBundleId")
                                ,metadata.getStr("artistId")
                                ,metadata.getStr("bundleShortVersionString"));

                        Console.log("[{}] 获取下载链接成功; fileName:{}, url:{}",trackName,fileName,url);

                        // 下载
                        HttpUtil.downloadFile(url, new File(filePath + fileName), new StreamProgress() {
                            @Override
                            public void start() {
                                Console.log("[{}] 开始下载...",trackName);
                            }
                            @Override
                            public void progress(long total, long progressSize) {
//                                Console.log("[{}] 已下载: {}/{}",trackName,progressSize,total);
                                Console.log("[{}] 总大小:{} 已下载：{}",trackName, FileUtil.readableFileSize(total),FileUtil.readableFileSize(progressSize));
                            }

                            @Override
                            public void finish() {
                                Console.log("[{}] 下载完成",trackName);
                            }
                        });

                        // zip
                        try {
                            Path zipPath = Paths.get(filePath + fileName);
                            File tmpFile = new File("tmp.plist");
                            FileUtil.appendUtf8String(metadata.toString(),tmpFile);
                            ZipUtil.append(zipPath,tmpFile.toPath());
                            FileUtil.del(tmpFile);
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            FileUtil.del(new File("tmp.plist"));
                        }

                    }
                }
            }
        }
    }

    private static void subscriptionDemo(){
        Account account = new Account();
//        account.setAccount("djli0506@163.com");
//        account.setPwd("!!B0527s0207!!");
        account.setAccount("qewqeq@2980.com");
        account.setPwd("dPFb6cSD41");

        String guid = DataUtil.getGuidByAppleId(account.getAccount());
        //HttpResponse authRsp = ITunesUtil.authenticate(account,"",guid);
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

    private static void redeemDemo(){
        Account account = new Account();
//        account.setAccount("djli0506@163.com");
//        account.setPwd("!!B0527s0207!!");
        account.setAccount("qewqeq@2980.com");
        account.setPwd("dPFb6cSD41");

        String guid = DataUtil.getGuidByAppleId(account.getAccount());
        //HttpResponse authRsp = ITunesUtil.authenticate(account,"",guid);
        HttpResponse authRsp = null;
        // 鉴权
        if (authRsp != null && authRsp.getStatus() == 200){
            String cardCode = "XMPC3HRMNM6K5FXP";
//            String cardCode = "erererrerewfrsf";
            HttpResponse redeemRsp = ITunesUtil.redeem(authRsp, guid, cardCode);
            if (redeemRsp.getStatus() == 200){
                JSONObject redeemBody = JSONUtil.parseObj(redeemRsp.body());
                System.err.println(redeemBody);
                Integer status = redeemBody.getInt("status");
                if (status != 0){
                    String userPresentableErrorMessage = redeemBody.getStr("userPresentableErrorMessage");
                    String message = "礼品卡[%s]兑换失败! %s";
                    Console.log(String.format(message,cardCode,userPresentableErrorMessage));
                }else{
                    String message = "礼品卡[%s]兑换成功!";
                    Console.log(String.format(message,cardCode));
                }
            }
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

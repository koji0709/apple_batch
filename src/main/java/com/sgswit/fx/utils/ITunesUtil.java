package com.sgswit.fx.utils;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

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
    public static void main(String[] args) {
//        accountPurchasesCount(null);
//        getPurchases(null);
//        getPaymentInfos(null);
        addOrEditBillingInfoSrv(null);
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
    }


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
        headers.put("X-Apple-I-MD-M",ListUtil.toList("Q2OtXd0fi23JpGxDq05FG3ROgkGa1HAx2VhWTWCQUjLiVAThbGjO1NSG4QvxyzyeL2OO8c/p9MkqPbMf"));
        headers.put("X-Apple-I-MD",ListUtil.toList("AAAABQAAABBODIv4BF0jb9+vO4yWoI2qAAAAAg=="));
        headers.put("X-Dsid",ListUtil.toList("8135448658"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));

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
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("p30-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));
        headers.put("Origin", ListUtil.toList("https://finance-app.itunes.apple.com"));

        headers.put("User-Agent",ListUtil.toList("iTunes/12.12.10 (Windows; Microsoft Windows 10 x64 (Build 19045); x64) AppleWebKit/7613.2007.1014.14 (dt:2)"));
        headers.put("X-Apple-I-MD-RINFO",ListUtil.toList("84215040"));
        headers.put("X-Apple-I-MD-M",ListUtil.toList("Q2OtXd0fi23JpGxDq05FG3ROgkEVQu92XZBrKZZSA2DlFswLisjD31ycS05u7hliBihILZlRF2bCOwq2"));
        headers.put("X-Apple-I-MD",ListUtil.toList("AAAABQAAABDi8d9/9w4AeM0Z1VLqcbepAAAAAg=="));
        headers.put("X-Dsid",ListUtil.toList("8135448658"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("X-Token",ListUtil.toList("AwIAAAECAAHZegAAAABlMIvLtZ5LKJL2njsKMx1eberxs2gAMdA="));
        String cookie="amp=BjFG4J4lKZ8lCZKbHk2R9VJQqpQ7iXDY0+5sm6C6vY3f9BLEvO7Qei9pWd0xnEkwDfoF5cttVFZBqPti1Vrk+ItIXPkkf/4E4P1W1XSZLPc=; mt-tkn-8135448658=AtPmOyVcYCBjcPvyWXrTknQOzMVFHE4g9CNiDKYD3RrL2hkVbMRQNm2cjCoTWkGVJnbajuSuvUF1X/XekDdqp9wqYKjoNVGFjteHjBE9U8jSAsXTM5jNhU6yRY9vz2g5YO2icRw1NoHJ3yCFQJmoBTsBw8MJ83nC4CCg5x6GBAlJuDylf8V29oqKYwHeTtm5UZcXzss=; mz_mt0-8135448658=AjRndqMG1vDGhUjeYzBc/4jnlzD/9SXb2tc8/Emn1AMmSuRr2sfxDH7n2xWiFnQK/CVDhynArimHE1zppzNH1ZENfEezBhGeJPw6B6J4uvPt1Of4zTvFUFs0VZXZjfPpPw4wpfgsW7DZAasLtpzlzowpXlsFJbqnnCb6oog1IgEOy/1XCViyqQ7szyzXbGfO+BPIFBg=; ampsc=nK2UkOwXl/iIQ7L+OUVz6pYRc3WYK+I6k7DuStxOyzE=; itspod=30; mz_at0-8135448658=AwQAAAECAAHZegAAAABlMIvLq3Nz21/eLAidiecxA1VcpbyE1pk=; mz_at_ssl-8135448658=AwUAAAECAAHZegAAAABlMIvLguB9HytFOeh2/Qt5dDmj/EUfAUs=; pldfltcid=33c1e4e8ac6640f6961f682af925fe1a030; wosid-lite=mjv2YAyFasofT6Y0Ycvl70; X-Dsid=8135448658; xp_ci=3z1Qb49BzBS5z5M4zAQsz1LuViweKL";

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
    　* 删除所有支付方式
      * @param
     * @param response
    　* @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
    　* @throws
    　* @author DeZh
    　* @date 2023/10/25 11:49
    */
    public  static Map<String,String> getPaymentInfos(HttpResponse response){
        Map<String,String> result=new HashMap<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Content-Type", ListUtil.toList("application/json; charset=UTF-8"));
        headers.put("Host", ListUtil.toList("p30-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));
        headers.put("Origin", ListUtil.toList("https://finance-app.itunes.apple.com"));
        headers.put("User-Agent",ListUtil.toList("iTunes/12.12.10 (Windows; Microsoft Windows 10 x64 (Build 19045); x64) AppleWebKit/7613.2007.1014.14 (dt:2)"));
        headers.put("X-Apple-I-MD-RINFO",ListUtil.toList("84215040"));
        headers.put("X-Apple-I-MD-M",ListUtil.toList("Q2OtXd0fi23JpGxDq05FG3ROgkEVQu92XZBrKZZSA2DlFswLisjD31ycS05u7hliBihILZlRF2bCOwq2"));
        headers.put("X-Apple-I-MD",ListUtil.toList("AAAABQAAABDi8d9/9w4AeM0Z1VLqcbepAAAAAg=="));
        headers.put("X-Dsid",ListUtil.toList("8135448658"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("X-Token",ListUtil.toList("AwIAAAECAAHZegAAAABlOJfilEK2op9puR0mWkNMNbkQemE67dE="));
        String cookie="amp=oz+QsprugugNdH1dOqMziD+7NvCu3VFV2kRYPsMiEIlb3hzkd/PsiDnk4JV67cWA5Je3PwIHiUiPIdGoCN0PJhtoUBNgSfNoxyITDAAzXtw=; itst=0; mt-tkn-8135448658=Ai3J5B6alaoTmz6Vm48bshOWYZhNUFol2BoFDaOs3JfvzKabPxOauNjBsex20hTmVDQi9Fx3bnfeG8u35zUhEFoLy7XNrr4yKAh1Bveid2muByG4G/coH1ocF2qA5qykNzSBEbvhHNadzQyDs7dRbk2uuB6GpnDKPdGcEwrS9k2WnOYoenymHasURo9geVUuetLqf2I=; mz_mt0-8135448658=AgW7tbN1lanI9hjtOLDaVbhkN6ni8hxRqgEI7SCiqCIFz5Gc1WSKVo4SMHGjkbsKNGykyDIU1FHNVEf6ThJwxR3P5ZHSgt3MHDEZSjQswhQ3LRBzt/asKQLyf1qEqUl2Y+f4LrQ6tWQOqL0lzzS9C+819PUZEyNviBxvQX05XDcp5wntHBk1AleGft1q2mqWWEnNmJA=; ampsc=1S1xXEF743014EgN2zwVoC1uUCvWW8kEvrQM29Lq7Pg=; itspod=30; mz_at0-8135448658=AwQAAAECAAHZegAAAABlOJfjEwDr2i1W4JUHHV0HeCJRCoQjdDc=; mz_at_ssl-8135448658=AwUAAAECAAHZegAAAABlOJfjHk0UnNC2UHDOJ7R0KmUj7LBhnfE=; pldfltcid=33c1e4e8ac6640f6961f682af925fe1a030; wosid-lite=SMrDJXV7QMq7HJtEIv3zKw; X-Dsid=8135448658; xp_ab=1#WqjkRLH+-2+p9nsgcq0#isj11bm+-2+oE0ebSx03#yNFpB6B+-2+EZQVyff00; xp_abc=oE0ebSx03; xp_ci=3z4Kdsj1zCuGz551z950zj3FC7lDW";
        HttpResponse httpResponse = HttpUtil.createRequest(Method.GET,"https://p30-buy.itunes.apple.com/account/stackable/paymentInfos?managePayments=true")
                .header(headers)
                .cookie(cookie)
                .execute();
        if(httpResponse.getStatus()==401){
            result.put("code","-1");
            result.put("message","未登录或登录超时");
        }else if(httpResponse.getStatus()==200){
            String paymentInfosJsonString= JSONUtil.parseObj(httpResponse.body()).getByPath("data.attributes.paymentInfos").toString();
            JSONArray jsonArray=JSONUtil.parseArray(paymentInfosJsonString);
            for(Object jsonObject:jsonArray){
                String paymentId= (String) ((JSONObject)jsonObject).getByPath("paymentId");
                String paymentMethodType= (String) ((JSONObject)jsonObject).getByPath("paymentMethodType");
                if(!"None".equalsIgnoreCase(paymentMethodType)){
                    String url= MessageFormat.format("https://p30-buy.itunes.apple.com/account/stackable/paymentInfos/{0}/delete",paymentId);
                    HttpResponse delHttpResponse = HttpUtil.createRequest(Method.POST,url)
                            .header(headers)
                            .cookie(cookie)
                            .execute();
                }
            }
            result.put("code","0");
            result.put("message","操作成功");
        }
        return result;
    }

    public  static Map<String,String> addOrEditBillingInfoSrv(HttpResponse response){
        Map<String,String> result=new HashMap<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded; charset=UTF-8"));
        headers.put("Host", ListUtil.toList("p30-buy.itunes.apple.com"));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));
        headers.put("Origin", ListUtil.toList("https://finance-app.itunes.apple.com"));
        headers.put("User-Agent",ListUtil.toList("iTunes/12.12.10 (Windows; Microsoft Windows 10 x64 (Build 19045); x64) AppleWebKit/7613.2007.1014.14 (dt:2)"));
        headers.put("X-Apple-I-MD-RINFO",ListUtil.toList("84215040"));
        headers.put("X-Apple-I-MD-M",ListUtil.toList("Q2OtXd0fi23JpGxDq05FG3ROgkEVQu92XZBrKZZSA2DlFswLisjD31ycS05u7hliBihILZlRF2bCOwq2"));
        headers.put("X-Apple-I-MD",ListUtil.toList("AAAABQAAABDi8d9/9w4AeM0Z1VLqcbepAAAAAg=="));
        headers.put("X-Dsid",ListUtil.toList("8135448658"));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("X-Token",ListUtil.toList("AwIAAAECAAHZegAAAABlOLXoltoOFu3Mj1DFABkqWdcFo7tV7Rc="));
        String cookie="amia-8135448658=VbD6WfraX7qNPxiqXkGNeborLN/fKBZLIig2ZBquHUIfEAmgEkWIICpHO2T45z2NnivzJkUEtNVYefzyO3Qecg==; amp=dc7TigY0wSfgYzXi2RZwVAFXNDCYHv79584bpDKdacvAr+hDMWPZ5Gikvo6EN1iDmqCNmnUY1lTXAls/m1hFcRpyG7te50J3g00xQnWU+fc=; itst=0; mt-tkn-8135448658=AiERWD12++HIPsVY8Yu0OQd+4I4ffVkiNSp2/ydvXPfIqSbFQxvun0fOquelALrMd+mOPn/jQqCJc+X/5Xk3oioeMUL4m3GXnZ+2eGKuNUlSK1QdSR3mRh08tPYlwn8y7Kw5B2FuqrOuRh8UqdeNJ5jIDPMNOdPc4ElYwtI7qf8jQ/WRSfx0Iml8P/kXAZKS95Tbnps=; mz_mt0-8135448658=AkACU2Dzl5d1EaWKqQfgllH3ED5YfkoO9ltKvPqwL91we0a0eeU5JjTX0zSNdGvP/pTOX4uCgKCzl1NM+woJD9Sn/o1KsyZDs2W72wqeN+bnxjoKuZR4JKlv5JGopsBHPoWl3I08N7ZpsmEX/EY2na0lQxflPHeD0KNlejSBum9tmBKoCat8dHR/bHQ133ML7dez7Nk=; woinst=-1; wosid=TZ7hkqUbKXVoxfoabjLiRM; ns-mzf-inst=240-29-443-113-14-9041-3300019-30-mr30; hsaccnt=1; mzf_in=3300019; session-store-id=3A65CBB1CB654F4BE485A647BB95BD4E; ampsc=6p3fFPeDyggHqtPRNIwzzIGDP7U/8HVg/Vr4ptu5mg0=; itspod=30; mz_at0-8135448658=AwQAAAECAAHZegAAAABlOLXoKQCwc4VyKu95DjkHFQXejTShp/o=; mz_at_ssl-8135448658=AwUAAAECAAHZegAAAABlOLXonXa9vKGzDKhHIOaOdRafsSoz9Xk=; pldfltcid=33c1e4e8ac6640f6961f682af925fe1a030; wosid-lite=bihHyQCVFFaFe8hIMYGAXw; X-Dsid=8135448658; xp_ab=1#WqjkRLH+-2+p9nsgcq0#isj11bm+-2+oE0ebSx03#yNFpB6B+-2+EZQVyff00; xp_abc=oE0ebSx03; xp_ci=3z4Kdsj1zCuGz551z950zj3FC7lDW";
        String creditCardNumber="4737029072047856";
        String creditCardExpirationMonth="10";
        String creditCardExpirationYear="2024";
        String creditVerificationNumber="350";
        String body=MessageFormat.format("paymentMethodType=CreditCard&needsTopUp=false&creditCardNumber={0}&isCameraInput=false&creditCardExpirationMonth={1}&creditCardExpirationYear={2}&creditVerificationNumber={3}",
                creditCardNumber,creditCardExpirationMonth,creditCardExpirationYear,creditVerificationNumber);
        HttpResponse httpResponse = HttpUtil.createRequest(Method.POST,"https://p30-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/addOrEditBillingInfoSrv")
                .header(headers)
                .cookie(cookie)
                .body(body)
                .execute();
        if(httpResponse.getStatus()==401){
            result.put("code","-1");
            result.put("message","未登录或登录超时");
        }else if(httpResponse.getStatus()==200){
            String bodyJson=httpResponse.body();
            int status= (int) JSONUtil.parseObj(bodyJson).getByPath("status");
            if(status==0){
                result.put("code","0");
                result.put("message","成功");
            }else{
                String validationResults= JSONUtil.parseObj(bodyJson).getByPath("result.validationResults").toString();
                JSONArray jsonArray=JSONUtil.parseArray(validationResults);
                StringBuffer stringBuffer=new StringBuffer();
                for(Object jsonObject:jsonArray){
                    String errorString= (String) ((JSONObject)jsonObject).getByPath("errorString");
                    stringBuffer.append(errorString);
                    stringBuffer.append("\n");
                }
                result.put("code","-1");
                result.put("message",stringBuffer.toString());
            }
        }
        return result;
    }
}

package com.sgswit.fx.utils;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.Account;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingUtil {

    private static String code = "200";

    // 获取产品
    public static Map<String,Object> getProd(Account account) throws Exception {
        Map<String,Object> prodMap = new HashMap<>();
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));

        headers.put("Sec-Fetch-Site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));

        HttpResponse accRes = cn.hutool.http.HttpUtil.createGet("https://www.apple.com/shop/iphone/accessories")
                .header(headers).execute();
        if(accRes.getStatus() != 200){
            code = "500";
        }
        System.out.println("------------------accessories-----------------------------------------------");
        System.out.println(accRes.getStatus());
        System.out.println(accRes.headers());

        CookieUtils.setCookiesToMap(accRes,account.getCookieMap());

        System.out.println("------------------accessories----------------------------------------------");

        Document doc = Jsoup.parse(accRes.body());
        Elements elements = doc.select("a[href^=/shop/product/MHJA3AM/A/20w-usb-c-power-adapter]");

        String productUrl = "https://www.apple.com" + elements.get(0).attr("href");
        System.out.println(productUrl);

        HttpResponse prodRes = cn.hutool.http.HttpUtil.createGet(productUrl)
                .header(headers).execute();

        if(prodRes.getStatus() != 200){
            code = "500";
        }
        System.out.println("------------------product-----------------------------------------------");
        System.out.println(prodRes.getStatus());
        System.out.println(prodRes.headers());

        CookieUtils.setCookiesToMap(prodRes,account.getCookieMap());

        System.out.println("------------------product----------------------------------------------");


        Document prodDoc = Jsoup.parse(prodRes.body());

        Elements pordElements = prodDoc.select("form[action^=/shop/pdpAddToBag]");
        Element item = pordElements.get(0);
        String action = item.attr("action");

        Elements scriptElements = prodDoc.select("script");
        for(Element e : scriptElements){
            if(e.html().trim().startsWith("document.cookie")){
                String as_sfa_cookie   = e.html().substring(e.html().indexOf("as_sfa"),e.html().indexOf("\";"));
                int split = as_sfa_cookie.indexOf(";");
                String[] as_sfa = as_sfa_cookie.substring(0,split).split("=");
                if(as_sfa.length < 2 ){
                    continue;
                }
                account.getCookieMap().put(as_sfa[0],as_sfa[1]);

            }

        }

        Elements inputs = pordElements.select("input");
        Map<String,Object> inputMap = new HashMap<>();
        for(Element input : inputs){
            inputMap.put(input.attr("name"),input.attr("value"));
        }


        HttpResponse atbRes = cn.hutool.http.HttpUtil.createGet("https://www.apple.com/shop/beacon/atb")
                .header(headers).execute();
        if(atbRes.getStatus() != 200){
            code = "500";
        }
        System.out.println("------------------beacon/atb-----------------------------------------------");
        System.out.println(atbRes.getStatus());
        System.out.println(atbRes.headers());

        if(null != atbRes.headers().get("Set-Cookie")) {
            for (String c : atbRes.headers().get("Set-Cookie")) {
                int split = c.indexOf(";");
                String[] atbCookie = c.substring(0, split).split("=");
                if (atbCookie.length < 2) {
                    continue;
                }
                System.out.println("-------cookies--------" + atbCookie);
                if("as_atb".equals(atbCookie[0])) {

                    account.getCookieMap().put(atbCookie[0],atbCookie[1]);

                    String atbtoke = atbCookie[1].substring(atbCookie[1].lastIndexOf("|")+1);
                    inputMap.put("atbtoken",atbtoke);
                }
            }
        }
        System.out.println("------------------beacon/atb----------------------------------------------");


        prodMap.put("prod","MHJA3AM/A");
        prodMap.put("url","https://www.apple.com" + action);
        prodMap.put("body",inputMap);
        prodMap.put("referer",productUrl);
        prodMap.put("code",code);
        return prodMap;
    }

    // 添加到购物车
    public static Map<String,Object> add2bag(Map<String,Object> pordMap,Account account) throws Exception {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));
        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("referer",ListUtil.toList(pordMap.get("referer").toString()));
        headers.put("Sec-Fetch-Site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));

        HttpResponse res =  cn.hutool.http.HttpUtil.createPost(pordMap.get("url").toString())
                .header(headers)
                .form((Map<String,Object>)pordMap.get("body"))
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .execute();

        if(res.getStatus() != 303){
            code = "500";
        }
        System.out.println("------------------pdpAddToBag-----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());

        CookieUtils.setCookiesToMap(res,account.getCookieMap());

        System.out.println("------------------pdpAddToBag----------------------------------------------");
        HashMap<String, Object> map = new HashMap<>();
        map.put("code",code);
        return map;
    }

    // 查看购物车
    public static Map<String,Map<String,Object>>  shopbag(Account account) throws Exception{

        Map<String,Map<String,Object>> dataMap = new HashMap<>();

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));

        headers.put("Sec-Fetch-Site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));

        HttpResponse res = HttpUtil.createGet("https://www.apple.com/shop/bag")
                .header(headers)
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .execute();
        if(res.getStatus() != 200){
            code = "500";
        }
        //System.out.println("------------------shopbag-----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());

        Document prodDoc = Jsoup.parse(res.body());

        Elements initDataElement = prodDoc.select("script[id=init_data]");

        JSONObject jo = JSONUtil.parseObj(initDataElement.html());

        Map<String,Object> xheaders = new HashMap<>();
        xheaders.put("x-aos-stk",jo.getByPath("meta.h.x-aos-stk").toString());
        xheaders.put("x-aos-model-page",jo.getByPath("meta.h.x-aos-model-page").toString());
        xheaders.put("modelVersion",jo.getByPath("meta.h.modelVersion").toString());
        xheaders.put("syntax",jo.getByPath("meta.h.syntax").toString());
        dataMap.put("header",xheaders);

        Map<String,Object> bodys = new HashMap<>();
        bodys.put("shoppingCart.recommendations.recommendedItem.part",jo.getByPath("shoppingCart.recommendations.recommendedItem.d.part").toString());

        List<String> items = (List<String>) jo.getByPath("shoppingCart.items.c");
        for(String item : items){
            String giftKey = "shoppingCart.items." + item + ".isIntentToGift";
            String giftKeyContent = "shoppingCart.items." + item + ".d.isIntentToGift";

            String quantityKey = "shoppingCart.items." + item + ".itemQuantity.quantity";
            String quantityKeyContent = "shoppingCart.items." + item + ".itemQuantity.d.quantity";

            bodys.put(giftKey,(Boolean)jo.getByPath(giftKeyContent));
            bodys.put(quantityKey,(Integer)jo.getByPath(quantityKeyContent));
        }

        bodys.put("shoppingCart.locationConsent.locationConsent",(Boolean)jo.getByPath("shoppingCart.locationConsent.d.locationConsent"));
        bodys.put("shoppingCart.summary.promoCode.promoCode",jo.getByPath("shoppingCart.summary.promoCode.d.promoCode").toString());
        bodys.put("shoppingCart.actions.fcscounter",jo.getByPath("shoppingCart.actions.d.fcscounter").toString());
        bodys.put("shoppingCart.actions.fcsdata",jo.getByPath("shoppingCart.actions.d.fcsdata").toString());

        dataMap.put("body",bodys);

        Map<String,Object> map = new HashMap<>();
        map.put("code",code);
        dataMap.put("code",map);
        System.out.println("---------datamap----------" + dataMap);
        System.out.println("------------------shopbag----------------------------------------------");
        return dataMap;
    }

    // 提交购物车
    public static Map<String,Object> checkoutCart(Map<String,Map<String,Object>> bag,Account account) throws Exception{

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Content-Type",ListUtil.toList("application/x-www-form-urlencoded"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("X-Requested-With",ListUtil.toList("Fetch"));

        headers.put("x-aos-stk",ListUtil.toList(bag.get("header").get("x-aos-stk").toString()));
        headers.put("s-aos-model-page",ListUtil.toList(bag.get("header").get("x-aos-model-page").toString()));
        headers.put("modelVersion",ListUtil.toList(bag.get("header").get("modelVersion").toString()));
        headers.put("syntax",ListUtil.toList(bag.get("header").get("syntax").toString()));

        HttpResponse res = cn.hutool.http.HttpUtil.createPost("https://www.apple.com/shop/bagx/checkout_now?_a=checkout&_m=shoppingCart.actions")
                .header(headers)
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .form(bag.get("body"))
                .execute();
        HashMap<String, Object> map = new HashMap<>();
        if(res.getStatus() != 200){
            code = "500";
        }
        System.out.println("------------------checkout-----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());
        System.out.println("------------------checkout----------------------------------------------");

        JSONObject jo = JSONUtil.parseObj(res.body());
        map.put("url",jo.getByPath("head.data.url").toString());
        map.put("code",code);
        return map;
    }

    //调登录页面
    public static Map<String,String> shopSignIn(String url,Account account) throws Exception{

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("Sec-Fetch-Site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));

        HttpResponse res = cn.hutool.http.HttpUtil.createGet(url)
                .header(headers)
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .execute();
        if(res.getStatus() != 200){
            code = "500";
        }
        System.out.println("------------------shopSignIn-----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());
        System.out.println("------------------shopSignIn----------------------------------------------");

        Document prodDoc = Jsoup.parse(res.body());

        Elements initDataElement = prodDoc.select("script[id=init_data]");
        JSONObject meta = JSONUtil.parseObj(initDataElement.html());

        Map<String,String> dataMap = new HashMap<>();

        String x_aos_model_page = (String) meta.getByPath("meta.h.x-aos-model-page");
        String x_aos_stk = (String)meta.getByPath("meta.h.x-aos-stk");
        String modelVersion = (String) meta.getByPath("meta.h.modelVersion");
        String  syntax = (String) meta.getByPath("meta.h.syntax");

        String  serviceKey = (String) meta.getByPath("signIn.customerLoginIDMS.d.serviceKey");
        String  serviceURL = (String) meta.getByPath("signIn.customerLoginIDMS.d.serviceURL");
        String  callbackSignInUrl = (String) meta.getByPath("signIn.customerLoginIDMS.d.callbackSignInUrl");

        dataMap.put("x-aos-model-page",x_aos_model_page);
        dataMap.put("x-aos-stk",x_aos_stk);
        dataMap.put("modelVersion",modelVersion);
        dataMap.put("syntax",syntax);
        dataMap.put("serviceKey",serviceKey);
        dataMap.put("serviceURL",serviceURL);
        dataMap.put("callbackSignInUrl",callbackSignInUrl);
        dataMap.put("code",code);
        return dataMap;
    }

    //回调applestore
    public static Map<String,String> callBack(Map<String,String> signInMap,Account account) throws Exception{

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("x-aos-model-page", ListUtil.toList(signInMap.get("x-aos-model-page")));
        headers.put("x-aos-stk",ListUtil.toList(signInMap.get("x-aos-stk")));
        headers.put("modelVersion",ListUtil.toList(signInMap.get("modelVersion")));
        headers.put("syntax",ListUtil.toList(signInMap.get("syntax")));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        Map<String,Object> paramMap = new HashMap<>();

        paramMap.put("deviceID","");
        paramMap.put("grantCode","");

        HttpResponse resp = cn.hutool.http.HttpUtil.createPost(signInMap.get("callbackSignInUrl"))
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .execute();

        if(resp.getStatus() != 200){
            code = "500";
        }
        System.out.println("------------------callBack-----------------------------------------------");

        System.out.println(resp.getStatus());
        System.out.println(resp.headers());

        CookieUtils.setCookiesToMap(resp,account.getCookieMap());

        System.out.println(resp.body());
        JSONObject jo = JSONUtil.parseObj(resp.body());
        String url = jo.getByPath("head.data.url").toString();
        String pltn = jo.getByPath("head.data.args.pltn").toString();
        System.out.println("------------------callBack-----------------------------------------------");

        Map<String,String> ret = new HashMap<>();
        ret.put("url",url);
        ret.put("pltn",pltn);
        ret.put("code",code);
        return ret;
    }

    //chechout start
    public static String checkoutStart(Map<String,String> checkoutStartMap,Account account) throws Exception{

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));


        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        Map<String,Object> paramMap = new HashMap<>();

        paramMap.put("pltn",checkoutStartMap.get("pltn"));

        HttpResponse resp = cn.hutool.http.HttpUtil.createPost(checkoutStartMap.get("url"))
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .execute();

        System.out.println("------------------checkoutStart-----------------------------------------------");

        System.out.println(resp.getStatus());
        System.out.println(resp.headers());
        System.out.println(resp.header("location"));

        System.out.println("------------------checkoutStart-----------------------------------------------");

        return resp.header("location").toString();

    }

    //提交
    public static Map<String,String> checkout(String checkoutUrl,Account account) throws Exception{

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));


        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        HttpResponse resp = cn.hutool.http.HttpUtil.createGet(checkoutUrl)
                .header(headers)
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .execute();

        if(resp.getStatus() != 200){
            code = "500";
        }
        System.out.println("------------------checkout-----------------------------------------------");

        System.out.println(resp.getStatus());
        System.out.println(resp.headers());

        System.out.println("------------------checkout-----------------------------------------------");

        Document prodDoc = Jsoup.parse(resp.body());

        Elements initDataElement = prodDoc.select("script[id=init_data]");
        JSONObject meta = JSONUtil.parseObj(initDataElement.html());

        Map<String,String> dataMap = new HashMap<>();

        String x_aos_model_page = (String) meta.getByPath("meta.h.x-aos-model-page");
        String x_aos_stk = (String)meta.getByPath("meta.h.x-aos-stk");
        String modelVersion = (String) meta.getByPath("meta.h.modelVersion");
        String  syntax = (String) meta.getByPath("meta.h.syntax");

        dataMap.put("x-aos-model-page",x_aos_model_page);
        dataMap.put("x-aos-stk",x_aos_stk);
        dataMap.put("modelVersion",modelVersion);
        dataMap.put("syntax",syntax);

        dataMap.put("url",checkoutUrl);
        dataMap.put("code",code);
        return dataMap;
    }

    //选择shipping - 邮寄
    public static String fillmentToShipping(Map<String,String> checkoutMap,Account account) throws Exception{

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("x-aos-model-page", ListUtil.toList(checkoutMap.get("x-aos-model-page")));
        headers.put("x-aos-stk",ListUtil.toList(checkoutMap.get("x-aos-stk")));
        headers.put("modelVersion",ListUtil.toList(checkoutMap.get("modelVersion")));
        headers.put("syntax",ListUtil.toList(checkoutMap.get("syntax")));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("checkout.fulfillment.deliveryTab.delivery.shipmentGroups.shipmentGroup-1.shipmentOptionsGroups.shipmentOptionsGroup-1.shippingOptions.selectShippingOption","E2");
        paramMap.put("checkout.fulfillment.fulfillmentOptions.selectFulfillmentLocation","HOME");

        String url =  checkoutMap.get("url") + "x?_a=continueFromFulfillmentToShipping&_m=checkout.fulfillment";

        HttpResponse resp = cn.hutool.http.HttpUtil.createPost(url)
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .execute();

        if(resp.getStatus() != 200){
            return "500";
        }
        System.out.println("------------------fillmentToShipping-----------------------------------------------");

        System.out.println(resp.getStatus());
        System.out.println(resp.headers());

        System.out.println("------------------fillmentToShipping-----------------------------------------------");
        return code;
    }

    //填写shipping - 地址
    public static Map<String,String> shippingToBilling(Map<String,String> checkoutMap,Account account) throws Exception{
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("x-aos-model-page", ListUtil.toList(checkoutMap.get("x-aos-model-page")));
        headers.put("x-aos-stk",ListUtil.toList(checkoutMap.get("x-aos-stk")));
        headers.put("modelVersion",ListUtil.toList(checkoutMap.get("modelVersion")));
        headers.put("syntax",ListUtil.toList(checkoutMap.get("syntax")));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        Map<String,Object> paramMap = new HashMap<>();

        String address = "美国";
        //TODO: 需要根据appleid 所属国家， 调整如下相关地址
        if("USA".equals(account.getCountry())){
            paramMap.put("checkout.shipping.addressNotification.address.emailAddress","");
            paramMap.put("checkout.shipping.addressSelector.selectAddress","newAddr");
            paramMap.put("checkout.shipping.addressSelector.newAddress.saveToAddressBook",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street2","zhichunjiayuan201hao");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.lastName","wang");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.firstName","pingping");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.companyName","");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street","29Chao4HaiLi701ST");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.isBusinessAddress",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.postalCode","97216-1701");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.zipLookupCityState","Portland, OR");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.countryCode","US");
        }


        String url = checkoutMap.get("url") + "x?_a=continueFromShippingToBilling&_m=checkout.shipping";

        HttpResponse resp = cn.hutool.http.HttpUtil.createPost(url)
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .execute();

        if(resp.getStatus() != 200){
            code = "500";
        }
        System.out.println("------------------checkoutStart-----------------------------------------------");

        System.out.println(resp.getStatus());
        System.out.println(resp.headers());

        System.out.println("------------------checkoutStart-----------------------------------------------");
        Map<String, String> map = new HashMap<>();
        map.put("code",code);
        map.put("address",address);
        return map;
    }

    //确认地址 - 显示账户余额
    public static HttpResponse selectedAddress(Map<String,String> checkoutMap,Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("x-aos-model-page", ListUtil.toList(checkoutMap.get("x-aos-model-page")));
        headers.put("x-aos-stk",ListUtil.toList(checkoutMap.get("x-aos-stk")));
        headers.put("modelVersion",ListUtil.toList(checkoutMap.get("modelVersion")));
        headers.put("syntax",ListUtil.toList(checkoutMap.get("syntax")));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        String url =  checkoutMap.get("url") + "x?_a=continueWithSelectedAddress&_m=checkout.shipping.addressVerification.selectedAddress";

        HttpResponse resp = HttpUtil.createPost(url)
                .header(headers)
                .cookie(MapUtil.join(account.getCookieMap(),";","=",true))
                .execute();

        System.out.println("------------------selectedAddress-----------------------------------------------");

        System.out.println(resp.getStatus());
        System.out.println(resp.headers());

        System.out.println("------------------selectedAddress-----------------------------------------------");

        JSONObject meta = JSONUtil.parseObj(resp.body());

        List<JSONObject> ja = (List<JSONObject>)meta.getByPath("body.checkout.billing.billingOptions.d.options");
        for(JSONObject o : ja){
            if(o.containsKey("disabled")){
                String disabled = o.get("disabled").toString();
                if("true".equals(disabled)){
                    System.out.println(o.get("disabledMessage"));
                    break;
                }
            }
        }

        String balance = meta.getByPath("body.checkout.billing.billingOptions.selectedBillingOptions.appleBalance.appleBalanceInput.d.availableAppleBalance").toString();
        String currency = meta.getByPath("body.checkout.billing.billingOptions.selectedBillingOptions.appleBalance.appleBalanceInput.d.currency").toString();
        System.out.println("----------balance---------" + balance);
        System.out.println("----------currentcy-------" + currency);
        return resp;
    }
}

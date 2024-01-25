package com.sgswit.fx.utils;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DELL
 */
public class ShoppingUtil {
    static String goodsCode="MHJA3AM/A";
    // 获取产品
    public static Map<String,Object> getProd( Map<String,Object> paras) throws Exception {
        String code2=MapUtil.getStr(paras,"code2","");
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));

        headers.put("Sec-Fetch-Site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));
        String accessoriesUrl="https://www.apple.com/"+code2+"/shop/iphone/accessories";
        HttpResponse accRes = HttpUtil.createGet(accessoriesUrl)
                .header(headers).execute();
        if(accRes.getStatus() != 200){
            paras.put("msg","商品信息加载失败！");
            paras.put("code","1");
            return paras;
        }
        Map<String,String> cookiesMap;
        if(null==paras.get("cookiesMap")){
            cookiesMap=new HashMap<>();
        }else{
            cookiesMap= (Map<String, String>) paras.get("cookiesMap");
        }
        cookiesMap=CookieUtils.setCookiesToMap(accRes,cookiesMap);
        paras.put("cookiesMap" , cookiesMap);
        Document doc = Jsoup.parse(accRes.body());
        Elements elements;
        if(StringUtils.isEmpty(code2)){
            elements = doc.select("a[href^=/shop/product/"+goodsCode+"/20w-usb-c-power-adapter]");
        }else{
            elements = doc.select("a[href^=/"+code2+"/shop/product/"+goodsCode+"/20w-usb-c-power-adapter]");
        }

        String productUrl = "https://www.apple.com" + elements.get(0).attr("href");

        HttpResponse prodRes = HttpUtil.createGet(productUrl)
                .header(headers).execute();

        if(prodRes.getStatus() != 200){
            paras.put("msg","商品信息加载失败！");
            paras.put("code","1");
            return paras;
        }
        cookiesMap=CookieUtils.setCookiesToMap(prodRes,cookiesMap);
        paras.put("cookiesMap" , cookiesMap);

        Document prodDoc = Jsoup.parse(prodRes.body());
        Elements pordElements;

        if(StringUtils.isEmpty(code2)){
            pordElements = prodDoc.select("form[action^=/shop/pdpAddToBag]");
        }else{
            pordElements = prodDoc.select("form[action^=/"+code2+"/shop/pdpAddToBag]");
        }

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
                cookiesMap.put(as_sfa[0],as_sfa[1]);
                paras.put("cookiesMap" , cookiesMap);
            }

        }

        Elements inputs = pordElements.select("input");
        Map<String,Object> inputMap = new HashMap<>();
        for(Element input : inputs){
            inputMap.put(input.attr("name"),input.attr("value"));
        }
        String atbUrl;
        if(StringUtils.isEmpty(code2)){
            atbUrl = "https://www.apple.com/shop/beacon/atb";
        }else{
            atbUrl = "https://www.apple.com/"+code2+"/shop/beacon/atb";
        }
        HttpResponse atbRes = cn.hutool.http.HttpUtil.createGet(atbUrl)
                .header(headers).execute();
        if(atbRes.getStatus() != 200){
            paras.put("msg","商品信息加载失败！");
            paras.put("code","1");
            return paras;
        }
        if(null != atbRes.headers().get("Set-Cookie")) {
            for (String c : atbRes.headers().get("Set-Cookie")) {
                int split = c.indexOf(";");
                String[] atbCookie = c.substring(0, split).split("=");
                if (atbCookie.length < 2) {
                    continue;
                }
                if("as_atb".equals(atbCookie[0])) {
                    cookiesMap.put(atbCookie[0],atbCookie[1]);
                    paras.put("cookiesMap" , cookiesMap);
                    String atbtoke = atbCookie[1].substring(atbCookie[1].lastIndexOf("|")+1);
                    inputMap.put("atbtoken",atbtoke);
                }
            }
        }
        paras.put("prod",goodsCode);
        if(StringUtils.isEmpty(code2)){
            paras.put("url","https://www.apple.com" + action);
        }else{
            paras.put("url","https://www.apple.com/"+code2 + action);
        }

        paras.put("body",inputMap);
        paras.put("referer",productUrl);
        paras.put("code",Constant.SUCCESS);
        return paras;
    }

    // 添加到购物车
    public static Map<String,Object> add2bag(Map<String,Object> paras) throws Exception {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));
        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("referer",ListUtil.toList(paras.get("referer").toString()));
        headers.put("Sec-Fetch-Site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));

        HttpResponse res = HttpUtil.createPost(paras.get("url").toString())
                .header(headers)
                .form((Map<String,Object>)paras.get("body"))
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .execute();

        if(res.getStatus() != 303){
            paras.put("msg","添加到购物车失败！");
            paras.put("code","1");
            return paras;
        }
        Map<String,String> cookiesMap;
        if(null==paras.get("cookiesMap")){
            cookiesMap=new HashMap<>();
        }else{
            cookiesMap= (Map<String, String>) paras.get("cookiesMap");
        }
        paras.put("cookiesMap" , CookieUtils.setCookiesToMap(res,cookiesMap));
        paras.put("code",Constant.SUCCESS);
        return paras;
    }

    // 查看购物车
    public static Map<String,Object>  shopbag(Map<String,Object> paras) throws Exception{
        Map<String,Object> map = new HashMap<>();

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));

        headers.put("Sec-Fetch-Site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));
        String bagUrl;
        String code2=MapUtil.getStr(paras,"code2");
        if(StringUtils.isEmpty(code2)){
            bagUrl="https://www.apple.com/shop/bag";
        }else{
            bagUrl="https://www.apple.com/"+code2+"/shop/bag";
        }

        HttpResponse res = HttpUtil.createGet(bagUrl)
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .execute();
        if(res.getStatus() != 200){
            paras.put("msg","获取购物车中商品信息失败！");
            paras.put("code","1");
            return paras;
        }
        Document prodDoc = Jsoup.parse(res.body());

        Elements initDataElement = prodDoc.select("script[id=init_data]");

        JSONObject jo = JSONUtil.parseObj(initDataElement.html());

        Map<String,Object> xheaders = new HashMap<>();
        xheaders.put("x-aos-stk",jo.getByPath("meta.h.x-aos-stk").toString());
        xheaders.put("x-aos-model-page",jo.getByPath("meta.h.x-aos-model-page").toString());
        xheaders.put("modelVersion",jo.getByPath("meta.h.modelVersion").toString());
        xheaders.put("syntax",jo.getByPath("meta.h.syntax").toString());
        paras.put("header",xheaders);

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
        paras.put("body",bodys);
        paras.put("code",Constant.SUCCESS);
        return paras;
    }

    // 提交购物车
    public static Map<String,Object> checkoutCart(Map<String,Object> paras) throws Exception{

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Content-Type",ListUtil.toList("application/x-www-form-urlencoded"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("X-Requested-With",ListUtil.toList("Fetch"));

        Map<String,Object> header= (Map<String, Object>) paras.get("header");

        headers.put("x-aos-stk",ListUtil.toList(header.get("x-aos-stk").toString()));
        headers.put("s-aos-model-page",ListUtil.toList(header.get("x-aos-model-page").toString()));
        headers.put("modelVersion",ListUtil.toList(header.get("modelVersion").toString()));
        headers.put("syntax",ListUtil.toList(header.get("syntax").toString()));

        HttpResponse res = cn.hutool.http.HttpUtil.createPost("https://www.apple.com/shop/bagx/checkout_now?_a=checkout&_m=shoppingCart.actions")
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .form(MapUtil.getStr(paras,"body"))
                .execute();
        if(res.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","购物信息提交失败");
            return paras;
        }
        JSONObject jo = JSONUtil.parseObj(res.body());
        paras.put("url",jo.getByPath("head.data.url").toString());
        paras.put("code",Constant.SUCCESS);
        return paras;
    }

    //调登录页面
    public static Map<String,Object> shopSignIn(Map<String,Object> paras) throws Exception{
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("Sec-Fetch-Site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));

        HttpResponse res = cn.hutool.http.HttpUtil.createGet(MapUtil.getStr(paras,"url"))
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .execute();
        if(res.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","登录页面加载失败");
            return paras;
        }
        Document prodDoc = Jsoup.parse(res.body());

        Elements initDataElement = prodDoc.select("script[id=init_data]");
        JSONObject meta = JSONUtil.parseObj(initDataElement.html());



        String x_aos_model_page = (String) meta.getByPath("meta.h.x-aos-model-page");
        String x_aos_stk = (String)meta.getByPath("meta.h.x-aos-stk");
        String modelVersion = (String) meta.getByPath("meta.h.modelVersion");
        String  syntax = (String) meta.getByPath("meta.h.syntax");

        String  serviceKey = (String) meta.getByPath("signIn.customerLoginIDMS.d.serviceKey");
        String  serviceURL = (String) meta.getByPath("signIn.customerLoginIDMS.d.serviceURL");
        String  callbackSignInUrl = (String) meta.getByPath("signIn.customerLoginIDMS.d.callbackSignInUrl");

        paras.put("x-aos-model-page",x_aos_model_page);
        paras.put("x-aos-stk",x_aos_stk);
        paras.put("modelVersion",modelVersion);
        paras.put("syntax",syntax);
        paras.put("serviceKey",serviceKey);
        paras.put("serviceURL",serviceURL);
        paras.put("callbackSignInUrl",callbackSignInUrl);
        paras.put("code",Constant.SUCCESS);
        return paras;
    }

    //回调applestore
    public static Map<String,Object> callBack(Map<String,Object> signInMap) throws Exception{
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("x-aos-model-page", ListUtil.toList(MapUtil.getStr(signInMap,"x-aos-model-page")));
        headers.put("x-aos-stk",ListUtil.toList(MapUtil.getStr(signInMap,"x-aos-stk")));
        headers.put("modelVersion",ListUtil.toList(MapUtil.getStr(signInMap,"modelVersion")));
        headers.put("syntax",ListUtil.toList(MapUtil.getStr(signInMap,"syntax")));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        Map<String,Object> paramMap = new HashMap<>();

        paramMap.put("deviceID","");
        paramMap.put("grantCode","");

        HttpResponse resp = cn.hutool.http.HttpUtil.createPost(MapUtil.getStr(signInMap,"callbackSignInUrl"))
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join((Map<String,String>) signInMap.get("cookiesMap"),";","=",true))
                .execute();

        if(resp.getStatus() != 200){
            signInMap.put("code","1");
            signInMap.put("msg","页面跳转失败");
            return signInMap;
        }
        CookieUtils.setCookiesToMap(resp,(Map<String,String>) signInMap.get("cookiesMap"));

        JSONObject jo = JSONUtil.parseObj(resp.body());
        String url = jo.getByPath("head.data.url").toString();
        String pltn = jo.getByPath("head.data.args.pltn").toString();
        signInMap.put("url",url);
        signInMap.put("pltn",pltn);
        signInMap.put("code",Constant.SUCCESS);
        return signInMap;
    }

    //chechout start
    public static Map<String,Object> checkoutStart(Map<String,Object> checkoutStartMap) throws Exception{

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

        HttpResponse resp = cn.hutool.http.HttpUtil.createPost(checkoutStartMap.get("url").toString())
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join((Map<String,String>) checkoutStartMap.get("cookiesMap"),";","=",true))
                .execute();
        checkoutStartMap.put("location",resp.header("location"));
        return checkoutStartMap;

    }

    //提交
    public static Map<String,Object> checkout(Map<String,Object> paras) throws Exception{
        String checkoutUrl=paras.get("location").toString();

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
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .execute();

        if(resp.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","提交操作失败");
            return paras;
        }
        Document prodDoc = Jsoup.parse(resp.body());

        Elements initDataElement = prodDoc.select("script[id=init_data]");
        JSONObject meta = JSONUtil.parseObj(initDataElement.html());
        String x_aos_model_page = (String) meta.getByPath("meta.h.x-aos-model-page");
        String x_aos_stk = (String)meta.getByPath("meta.h.x-aos-stk");
        String modelVersion = (String) meta.getByPath("meta.h.modelVersion");
        String  syntax = (String) meta.getByPath("meta.h.syntax");

        paras.put("x-aos-model-page",x_aos_model_page);
        paras.put("x-aos-stk",x_aos_stk);
        paras.put("modelVersion",modelVersion);
        paras.put("syntax",syntax);

        paras.put("url",checkoutUrl);
        paras.put("code",Constant.SUCCESS);
        return paras;
    }

    //选择shipping - 邮寄
    public static Map<String,Object> fillmentToShipping(Map<String,Object> paras) throws Exception{

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("x-aos-model-page", ListUtil.toList(paras.get("x-aos-model-page").toString()));
        headers.put("x-aos-stk",ListUtil.toList(paras.get("x-aos-stk").toString()));
        headers.put("modelVersion",ListUtil.toList(paras.get("modelVersion").toString()));
        headers.put("syntax",ListUtil.toList(paras.get("syntax").toString()));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("checkout.fulfillment.deliveryTab.delivery.shipmentGroups.shipmentGroup-1.shipmentOptionsGroups.shipmentOptionsGroup-1.shippingOptions.selectShippingOption","E2");
        paramMap.put("checkout.fulfillment.fulfillmentOptions.selectFulfillmentLocation","HOME");

        String url =  paras.get("url") + "x?_a=continueFromFulfillmentToShipping&_m=checkout.fulfillment";

        HttpResponse resp = cn.hutool.http.HttpUtil.createPost(url)
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .execute();

        if(resp.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","余额查询失败");
            return paras;
        }
        paras.put("code",Constant.SUCCESS);
        return paras;
    }

    //填写shipping - 地址
    public static Map<String,Object> shippingToBilling(Map<String,Object> paras) throws Exception{
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("x-aos-model-page", ListUtil.toList(paras.get("x-aos-model-page").toString()));
        headers.put("x-aos-stk",ListUtil.toList(paras.get("x-aos-stk").toString()));
        headers.put("modelVersion",ListUtil.toList(paras.get("modelVersion").toString()));
        headers.put("syntax",ListUtil.toList(paras.get("syntax").toString()));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        Map<String,Object> paramMap = new HashMap<>();

        String countryCode=MapUtil.getStr(paras,"countryCode");
        //TODO: 需要根据appleid 所属国家， 调整如下相关地址
        if("USA".equals(countryCode)){
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
        }else if("JPN".equals(countryCode)){
            paramMap.put("checkout.shipping.addressNotification.address.emailAddress","");
            paramMap.put("checkout.shipping.addressSelector.selectAddress","newAddr");
            paramMap.put("checkout.shipping.addressSelector.newAddress.saveToAddressBook",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street2","zhichunjiayuan201hao");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.lastName","wang");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.firstName","pingping");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.companyName","");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street","番地など");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.isBusinessAddress",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.postalCode","951-8073");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.city","新宿区");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.state","山形県");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.countryCode","JP");
        }else if("DEU".equals(countryCode)){
            paramMap.put("checkout.shipping.addressNotification.address.emailAddress","");
            paramMap.put("checkout.shipping.addressSelector.selectAddress","newAddr");
            paramMap.put("checkout.shipping.addressSelector.newAddress.saveToAddressBook",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street2","99085 Erfurt");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.lastName","wang");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.firstName","pingping");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.companyName","");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street","Thälmannstraße 51");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.isBusinessAddress",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.postalCode","68089");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.city","Erfurt");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.countryCode","DE");
        }else if("AUS".equals(countryCode)){
            paramMap.put("checkout.shipping.addressNotification.address.emailAddress","");
            paramMap.put("checkout.shipping.addressSelector.selectAddress","newAddr");
            paramMap.put("checkout.shipping.addressSelector.newAddress.saveToAddressBook",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street2","zhichunjiayuan201hao");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.lastName","wang");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.firstName","pingping");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.companyName","");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street","29Chao4HaiLi701ST");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.isBusinessAddress",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.postalCode","8000");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.city","Victoria");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.state","VIC");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.countryCode","AU");
        }else if("CAN".equals(countryCode)){
            paramMap.put("checkout.shipping.addressNotification.address.emailAddress","");
            paramMap.put("checkout.shipping.addressSelector.selectAddress","newAddr");
            paramMap.put("checkout.shipping.addressSelector.newAddress.saveToAddressBook",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street2","");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.lastName","wang");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.firstName","pingping");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.companyName","");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street","11755 108 Ave NW");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.isBusinessAddress",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.postalCode","V0T 1H0");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.cityTypeAhead.city","Edmonton");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.state","AB");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.countryCode","CA");
        }else if("GBR".equals(countryCode)){
            paramMap.put("checkout.shipping.addressNotification.address.emailAddress","");
            paramMap.put("checkout.shipping.addressSelector.selectAddress","newAddr");
            paramMap.put("checkout.shipping.addressSelector.newAddress.saveToAddressBook",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.street2","94 Broadway");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.lastName","wang");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.firstName","pingping");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.companyName","");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.street","Swanley");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.isBusinessAddress",false);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.postalCode","EC7I 5OD");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.city","London, England");
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.countryCode","GB");
        }

        String nameByCountryCode = DataUtil.getNameByCountryCode(countryCode);

        String url = paras.get("url") + "x?_a=continueFromShippingToBilling&_m=checkout.shipping";

        HttpResponse resp = HttpUtil.createPost(url)
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .execute();
        if(resp.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","填写shipping地址失败");
            return paras;
        }
        paras.put("code",Constant.SUCCESS);
        paras.put("address",nameByCountryCode);
        return paras;
    }

    //确认地址 - 显示账户余额
    public static HttpResponse selectedAddress(Map<String,Object> paras) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0."));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("x-aos-model-page", ListUtil.toList(paras.get("x-aos-model-page").toString()));
        headers.put("x-aos-stk",ListUtil.toList(paras.get("x-aos-stk").toString()));
        headers.put("modelVersion",ListUtil.toList(paras.get("modelVersion").toString()));
        headers.put("syntax",ListUtil.toList(paras.get("syntax").toString()));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        String url =  paras.get("url") + "x?_a=continueWithSelectedAddress&_m=checkout.shipping.addressVerification.selectedAddress";

        HttpResponse resp = HttpUtil.createPost(url)
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .execute();
        JSONObject meta = JSONUtil.parseObj(resp.body());

        List<JSONObject> ja = (List<JSONObject>)meta.getByPath("body.checkout.billing.billingOptions.d.options");
        for(JSONObject o : ja){
            if(o.containsKey("disabled")){
                String disabled = o.get("disabled").toString();
                if("true".equals(disabled)){
                    break;
                }
            }
        }
        return resp;
    }
}

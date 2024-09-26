package com.sgswit.fx.utils;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.javafaker.Faker;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.BaseAreaInfo;
import com.sgswit.fx.utils.proxy.ProxyUtil;
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
    static String goodsCode="MUJT3AM/A";
    // 获取产品
    public static Map<String,Object> getProd( Map<String,Object> paras) throws Exception {

        String code2=MapUtil.getStr(paras,"code2","");
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));

        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("document"));
        headers.put("Sec-Fetch-User", ListUtil.toList("?1"));
        String accessoriesUrl;
        if(StringUtils.isEmpty(code2)){
            accessoriesUrl="https://www.apple.com/shop/watch/bands";
        }else{
            accessoriesUrl="https://www.apple.com/"+code2+"/shop/watch/bands";
        }
        HttpResponse accRes = ProxyUtil.execute(HttpUtil.createGet(accessoriesUrl)
                .header(headers));

        if (accRes.getStatus() == 301){
            String location = accRes.header("Location");
            code2 = "uk";
            paras.put("code2",code2);
            accRes = ProxyUtil.execute(HttpUtil.createGet("https://www.apple.com" + location)
                    .header(headers));
        }

        if(accRes.getStatus() != 200){
            paras.put("msg","商城信息加载失败！");
            paras.put("code","1");
            return paras;
        }
        Map<String,String> cookiesMap= new HashMap<>();
        cookiesMap=CookieUtils.setCookiesToMap(accRes,cookiesMap);
        paras.put("cookiesMap" , cookiesMap);
        Document doc = Jsoup.parse(accRes.body());
        Elements elements;
        if(StringUtils.isEmpty(code2)){
            //elements = doc.select("a[href^=/shop/product/"+goodsCode+"/40mm-black-unity-sport-loop]");
            elements = doc.select("a[href^=/shop/product/]");
        }else{
            //elements = doc.select("a[href^=/"+code2+"/shop/product/"+goodsCode+"40mm-black-unity-sport-loop]");
            elements = doc.select("a[href^=/"+code2+"/shop/product/]");
        }

        String productUrl = "https://www.apple.com" + elements.get(0).attr("href");

        HttpResponse prodRes = ProxyUtil.execute(HttpUtil.createGet(productUrl)
                .header(headers));

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
        HttpRequest httpRequest=HttpUtil.createGet(atbUrl)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .header(headers);
        HttpResponse atbRes = ProxyUtil.execute(httpRequest);
        if(atbRes.getStatus() != 200){
            paras.put("msg","商品信息加载失败！");
            paras.put("code","1");
            return paras;
        }
        cookiesMap=CookieUtils.setCookiesToMap(atbRes,cookiesMap);
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
        paras.put("url","https://www.apple.com" + action);
//        if(StringUtils.isEmpty(code2)){
//            paras.put("url","https://www.apple.com" + action);
//        }else{
//            paras.put("url","https://www.apple.com/"+code2 + action);
//        }

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
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("referer",ListUtil.toList(paras.get("referer").toString()));
        headers.put("Sec-Fetch-Site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));
        HttpRequest httpRequest=HttpUtil.createPost(paras.get("url").toString())
                .header(headers)
                .form((Map<String,Object>)paras.get("body"))
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true));
        HttpResponse res = ProxyUtil.execute(httpRequest);
        if(res.getStatus() == 302){
            String location=res.header("Location");
            paras.put("msg","加入购物车失败！"+res.getStatus());
            paras.put("code","302");
            return paras;
        }else if(res.getStatus() != 303){
            paras.put("msg","加入购物车失败！"+res.getStatus());
            paras.put("code","1");
            return paras;
        }
        Map<String,String>  cookiesMap= (Map<String, String>) paras.get("cookiesMap");;
        paras.put("cookiesMap" , CookieUtils.setCookiesToMap(res,cookiesMap));
        paras.put("code",Constant.SUCCESS);
        return paras;
    }

    // 查看购物车
    public static Map<String,Object>  shopbag(Map<String,Object> paras){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));

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
        HttpRequest httpRequest=HttpUtil.createGet(bagUrl)
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true));
        HttpResponse res = ProxyUtil.execute(httpRequest);
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
        String part=jo.getByPath("shoppingCart.recommendations.recommendedItem.d.part",String.class);
        bodys.put("shoppingCart.recommendations.recommendedItem.part",part);

        List<String> items = (List<String>) jo.getByPath("shoppingCart.items.c");
        for(String item : items){
            String giftKey = "shoppingCart.items." + item + ".isIntentToGift";
            String giftKeyContent = "shoppingCart.items." + item + ".d.isIntentToGift";

            String quantityKey = "shoppingCart.items." + item + ".itemQuantity.quantity";
            String quantityKeyContent = "shoppingCart.items." + item + ".itemQuantity.d.quantity";

            bodys.put(giftKey,jo.getByPath(giftKeyContent,Boolean.class));
            bodys.put(quantityKey,jo.getByPath(quantityKeyContent,Integer.class));
        }

        bodys.put("shoppingCart.locationConsent.locationConsent",jo.getByPath("shoppingCart.locationConsent.d.locationConsent",Boolean.class));
        bodys.put("shoppingCart.summary.promoCode.promoCode",jo.getByPath("shoppingCart.summary.promoCode.d.promoCode").toString());
        bodys.put("shoppingCart.actions.fcscounter",jo.getByPath("shoppingCart.actions.d.fcscounter").toString());
        bodys.put("shoppingCart.actions.fcsdata",jo.getByPath("shoppingCart.actions.d.fcsdata").toString());
        paras.put("body",bodys);
        paras.put("code",Constant.SUCCESS);
        Map<String,String>  cookiesMap= (Map<String, String>) paras.get("cookiesMap");;
        paras.put("cookiesMap" , CookieUtils.setCookiesToMap(res,cookiesMap));
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
        String code2 = MapUtil.getStr(paras, "code2", "");
        String host = StringUtils.isEmpty(code2) ? "https://www.apple.com" : "https://www.apple.com/" + code2;
        HttpRequest httpRequest=HttpUtil.createPost(host + "/shop/bagx/checkout_now?_a=checkout&_m=shoppingCart.actions")
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .form(MapUtil.getStr(paras,"body"));
        HttpResponse res = ProxyUtil.execute(httpRequest);
        if(res.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","购物信息提交失败");
            return paras;
        }
        JSONObject jo = JSONUtil.parseObj(res.body());
        paras.put("url",jo.getByPath("head.data.url").toString());
        paras.put("code",Constant.SUCCESS);
        Map<String,String>  cookiesMap= (Map<String, String>) paras.get("cookiesMap");;
        paras.put("cookiesMap" , CookieUtils.setCookiesToMap(res,cookiesMap));
        return paras;
    }

    //调登录页面
    public static Map<String,Object> shopSignIn(Map<String,Object> paras){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.017"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("Referer",ListUtil.toList("https://www.apple.com/shop/bag"));
        HttpRequest httpRequest=HttpUtil.createGet(MapUtil.getStr(paras,"url"))
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true));
        HttpResponse res = ProxyUtil.execute(httpRequest);
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
        paras.put("checkoutStartRefererUrl",MapUtil.getStr(paras,"url"));
        paras.put("code",Constant.SUCCESS);
        return paras;
    }

    //回调applestore
    public static Map<String,Object> callBack(Map<String,Object> signInMap) throws Exception{
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.9"));
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
        HttpRequest httpRequest=HttpUtil.createPost(MapUtil.getStr(signInMap,"callbackSignInUrl"))
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join((Map<String,String>) signInMap.get("cookiesMap"),";","=",true));
        HttpResponse resp = ProxyUtil.execute(httpRequest);
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
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));
        headers.put("Upgrade-Insecure-Requests", ListUtil.toList("1"));
        headers.put("sec-fetch-dest",ListUtil.toList("document"));
        headers.put("sec-fetch-mode",ListUtil.toList("navigate"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));
        headers.put("Referer",ListUtil.toList(MapUtil.getStr(checkoutStartMap,"checkoutStartRefererUrl")));

        Map<String,Object> paramMap = new HashMap<>();

        paramMap.put("pltn",checkoutStartMap.get("pltn"));
        HttpRequest httpRequest=HttpUtil.createPost(checkoutStartMap.get("url").toString())
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join((Map<String,String>) checkoutStartMap.get("cookiesMap"),";","=",true));
        HttpResponse resp = ProxyUtil.execute(httpRequest);
        checkoutStartMap.put("location",resp.header("location"));
        Map<String,String>  cookiesMap= (Map<String, String>) checkoutStartMap.get("cookiesMap");;
        checkoutStartMap.put("cookiesMap" , CookieUtils.setCookiesToMap(resp,cookiesMap));
        return checkoutStartMap;

    }

    //提交
    public static Map<String,Object> checkout(Map<String,Object> paras) throws Exception{
        String checkoutUrl=paras.get("location").toString();

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));


        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        HttpRequest httpRequest=HttpUtil.createGet(checkoutUrl)
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true));
        HttpResponse resp = ProxyUtil.execute(httpRequest);
        if(resp.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","提交操作失败");
            return paras;
        }
        Document prodDoc = Jsoup.parse(resp.body());

        Element initDataElement = prodDoc.selectFirst("script[id=init_data]");
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


        Elements metricsDataElement = prodDoc.select("script[id=\"metrics\"]");

        JSONObject jsonObject = JSONUtil.parseObj(metricsDataElement.html());
        String leadQuoteTime=jsonObject.getByPath("data.properties.leadQuoteTime",String.class);
        //正常格式是："leadQuoteTime": "AOS: CHECKOUT|MUJT3|0|postalCode=99613|Delivery|E2|"
        if (leadQuoteTime.contains("Delivery")){
            paras.put("deliveryFlag",true);
        }else {
            paras.put("deliveryFlag",false);
        }
        String[] split = leadQuoteTime.split("\\|");
        paras.put("selectShippingOption",split[split.length-1]);
        return paras;
    }

    //选择shipping - 邮寄
    public static Map<String,Object> fulfillmentTodeliveryTab(Map<String,Object> paras){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, *; q=0.01"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.9"));
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
        headers.put("referer",ListUtil.toList(paras.get("url")+"?_s=Shipping-init"));
        headers.put("Upgrade-Insecure-Requests",ListUtil.toList("1"));
        Map<String,Object> paramMap = new HashMap<>();
        Map<String,String> addressInfo=MapUtil.get(paras,"addressInfo",Map.class);
        String postalCode=addressInfo.get("addressOfficialPostalCode");
        paramMap.put("checkout.fulfillment.deliveryTab.delivery.deliveryLocation.address.postalCode",postalCode);
        String url =  paras.get("url") + "x?_a=calculate&_m=checkout.fulfillment.deliveryTab.delivery.deliveryLocation.address";
        HttpRequest httpRequest=HttpUtil.createPost(url)
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true));
        HttpResponse resp = ProxyUtil.execute(httpRequest);
        if(resp.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","余额查询失败，请稍后重试！");
            return paras;
        }
        paras.put("code",Constant.SUCCESS);
        Map<String,String>  cookiesMap= (Map<String, String>) paras.get("cookiesMap");;
        paras.put("cookiesMap" , CookieUtils.setCookiesToMap(resp,cookiesMap));
        return paras;
    }
    //选择shipping - 邮寄
    public static Map<String,Object> fillmentToShipping(Map<String,Object> paras) throws Exception{
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, *; q=0.01"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.9"));
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
        headers.put("referer",ListUtil.toList(paras.get("url")+"?_s=Shipping-init"));
        headers.put("Upgrade-Insecure-Requests",ListUtil.toList("1"));
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("checkout.fulfillment.deliveryTab.delivery.shipmentGroups.shipmentGroup-1.shipmentOptionsGroups.shipmentOptionsGroup-1.shippingOptions.selectShippingOption",paramMap.get("selectShippingOption"));
        paramMap.put("checkout.fulfillment.fulfillmentOptions.selectFulfillmentLocation","HOME");

        String url =  paras.get("url") + "x?_a=continueFromFulfillmentToShipping&_m=checkout.fulfillment";
        HttpRequest httpRequest=HttpUtil.createPost(url)
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true));
        HttpResponse resp = ProxyUtil.execute(httpRequest);
        if(resp.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","余额查询失败，请稍后重试！");
            return paras;
        }
        paras.put("code",Constant.SUCCESS);
        Map<String,String>  cookiesMap= (Map<String, String>) paras.get("cookiesMap");;
        paras.put("cookiesMap" , CookieUtils.setCookiesToMap(resp,cookiesMap));
        return paras;
    }

    //填写shipping - 地址
    public static Map<String,Object> shippingToBilling(Map<String,Object> paras) throws Exception{
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, *; q=0.01"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.9"));
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
        headers.put("referer",ListUtil.toList(paras.get("url")+"?_s=Shipping-init"));
        headers.put("Upgrade-Insecure-Requests",ListUtil.toList("1"));
        Map<String,Object> paramMap = new HashMap<>();

        String countryCode=MapUtil.getStr(paras,"countryCode");
        //TODO: 需要根据appleid 所属国家， 调整如下相关地址
        paramMap.put("checkout.shipping.addressSelector.newAddress.saveToAddressBook",false);
        paramMap.put("checkout.shipping.addressSelector.newAddress.address.isBusinessAddress",false);
        paramMap.put("checkout.shipping.addressSelector.selectAddress","newAddr");
        paramMap.put("checkout.shipping.addressSelector.newAddress.address.companyName","");
        paramMap.put("checkout.shipping.addressNotification.address.emailAddress","");
        Faker faker=new Faker();
        paramMap.put("checkout.shipping.addressSelector.newAddress.address.lastName",faker.name().lastName());
        paramMap.put("checkout.shipping.addressSelector.newAddress.address.firstName",faker.name().firstName());
        BaseAreaInfo areaInfo=DataUtil.getInfoByCountryCode(countryCode);
        Map<String,String> addressInfo=MapUtil.get(paras,"addressInfo",Map.class);
        String postalCode=addressInfo.get("addressOfficialPostalCode");
        String street=addressInfo.get("addressOfficialLineFirst");
        String street2=addressInfo.get("addressOfficialLineSecond");
        String state=addressInfo.get("addressOfficialStateProvince");
        String city=addressInfo.get("addressOfficialCity");

//        if(!MapUtil.getBool(paras ,"deliveryFlag")){
            if("USA".equals(countryCode)){
                paramMap.put("checkout.shipping.addressContactPhone.address.fullDaytimePhone","4103562000");
                paramMap.put("checkout.shipping.addressSelector.newAddress.address.state",state);
                paramMap.put("checkout.shipping.addressSelector.newAddress.address.postalCode",postalCode);
                paramMap.put("checkout.shipping.addressSelector.newAddress.address.city",city);
                paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.city",state);
                paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.state",state);
            }
//        }
        if("USA".equals(countryCode) || "JPN".equals(countryCode) ||"DEU".equals(countryCode)
            ||"CAN".equals(countryCode)){
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street2",street2);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street",street);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.postalCode",postalCode);
        }
        if("USA".equals(countryCode)){
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.zipLookupCityState",city+", "+state);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.countryCode",areaInfo.getCode2());
        }else if("JPN".equals(countryCode)){
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.city",city);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.state",state);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.countryCode",areaInfo.getCode2());
        }else if("DEU".equals(countryCode)){
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.city",city);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.countryCode",areaInfo.getCode2());
        }else if("AUS".equals(countryCode)){
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street2",street2);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.street",street);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.postalCode",postalCode);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.city",city);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.state",state);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.zipLookup.countryCode",areaInfo.getCode2());
        }else if("CAN".equals(countryCode)){
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.cityTypeAhead.city",city);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.state",state);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.countryCode",areaInfo.getCode2());
        }else if("GBR".equals(countryCode)){
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.street2",street2);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.street",street);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.postalCode",postalCode);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.city",city+", "+state);
            paramMap.put("checkout.shipping.addressSelector.newAddress.address.addressLookup.fieldList.countryCode",areaInfo.getCode2());
        }

        String url = paras.get("url") + "x?_a=continueFromShippingToBilling&_m=checkout.shipping";
        HttpRequest httpRequest=HttpUtil.createPost(url)
                .header(headers)
                .form(paramMap)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true));
        HttpResponse resp = ProxyUtil .execute(httpRequest);
        if(resp.getStatus() != 200){
            paras.put("code","1");
            paras.put("msg","填写shipping地址失败");
            return paras;
        }
        paras.put("code",Constant.SUCCESS);
        paras.put("address",areaInfo.getNameZh());
        paras.put("resp",resp);
        Map<String,String>  cookiesMap= (Map<String, String>) paras.get("cookiesMap");;
        paras.put("cookiesMap" , CookieUtils.setCookiesToMap(resp,cookiesMap));
        return paras;
    }
    //确认地址 - 显示账户余额
    public static HttpResponse selectedAddress(Map<String,Object> paras) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.9"));
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
        headers.put("referer",ListUtil.toList(paras.get("url")+"?_s=Shipping-init"));
        headers.put("Upgrade-Insecure-Requests",ListUtil.toList("1"));
        String url =  paras.get("url") + "x/shipping?_a=continueWithSelectedAddress&_m=checkout.shipping.addressVerification.selectedAddress";
        HttpRequest httpRequest=HttpUtil.createPost(url)
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true));
        HttpResponse resp = ProxyUtil.execute(httpRequest);
        return resp;
    }
}

package com.sgswit.fx.utils.web;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.utils.CookieUtils;
import com.sgswit.fx.utils.StrUtils;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiftCardUtil {
    private static final String BROWSER_USER_AGENT="Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0";
    private static final String BROWSER_CLIENT_INFO="{\"U\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Nla44j1e3NlY5BNlY5BSmHACVZXnNA9.jMhquxilVVjpidPNs0oje9zH_y37lYAU.6elV2pNK1cllNIZ_wc6uTfwjNNlY5BNp55BNlan0Os5Apw.5ji\"}";

    /**
     * 加载初始化地址 https://secure.store.apple.com/shop/giftcard/balance
     * @return
     */
    public static HttpResponse initBalance(String countryCode) throws InterruptedException {
        // 每一步都检测中断信号
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("secure.store.apple.com"));
        headers.put("User-Agent", ListUtil.toList(BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Upgrade-Insecure-Requests", ListUtil.toList("1"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("document"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("none"));
        headers.put("Sec-Fetch-User", ListUtil.toList("?1"));
        headers.put("Priority", ListUtil.toList("u=4"));
        String url="https://secure.store.apple.com/shop/giftcard/balance";
        if(StrUtil.isNotBlank(countryCode) &&!"us".equalsIgnoreCase(countryCode)){
            url="https://secure.store.apple.com/"+countryCode.toLowerCase()+"/shop/giftcard/balance";
        }
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers));
        return res;
    }
    /**
     * 加载带通道的初始化地址 https://secure7.store.apple.com/shop/giftcard/balance
     * @return
     */
    public static HttpResponse initBalanceWithTunes(HttpResponse initBalanceResponse) throws InterruptedException {
        // 每一步都检测中断信号
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        HashMap<String, List<String>> headers = new HashMap<>();
        String locationUrl=initBalanceResponse.header("Location");
        headers.put("Host", ListUtil.toList(StrUtils.getHostFromUrl(locationUrl)));
        headers.put("User-Agent", ListUtil.toList(BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Upgrade-Insecure-Requests", ListUtil.toList("1"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("document"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("none"));
        headers.put("Sec-Fetch-User", ListUtil.toList("?1"));
        headers.put("Priority", ListUtil.toList("u=0, i"));
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(initBalanceResponse.header("Location"))
                        .header(headers)
                        .cookie(CookieUtils.getCookiesFromHeader(initBalanceResponse)));
        return res;
    }

    /**
     * 加载初始化登录地址https://secure7.store.apple.com/shop/signIn?ssi=4AAABmjUjHxQBIIO0gZcH6x7ikM3mZ3SJiAqbEic7zmw5JFtysISXABgTAAAAOGh0dHBzOi8vc2VjdXJlNy5zdG9yZS5hcHBsZS5jb20vc2hvcC9naWZ0Y2FyZC9iYWxhbmNlfHx8AAIBMFtI1FH_BF4DB6Hgeg0TN6SAGRS-bBaCWhFgGOO1YFE
     * @return
     */
    public static HttpResponse shopSignInInit(HttpResponse initBalanceWitTunesRes,Map<String,Object> authParas) throws InterruptedException {
        // 每一步都检测中断信号
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        HashMap<String, List<String>> headers = new HashMap<>();
        String locationUrl=initBalanceWitTunesRes.header("Location");
        headers.put("Host", ListUtil.toList(StrUtils.getHostFromUrl(locationUrl)));
        headers.put("User-Agent", ListUtil.toList(BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Upgrade-Insecure-Requests", ListUtil.toList("1"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("document"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("none"));
        headers.put("Sec-Fetch-User", ListUtil.toList("?1"));
        headers.put("Priority", ListUtil.toList("u=0, i"));
        String cookiesStr=CookieUtils.getCookiesFromHeader(initBalanceWitTunesRes)+";as_pcts="+MapUtil.getStr(authParas,"as_pcts_cookies");
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(locationUrl)
                .header(headers)
                .cookie(cookiesStr));
        return res;
    }

    public static Map<String,Object> parseShopSignInInitResponseDocument(HttpResponse shopSignInInitResponse){
        Map<String,Object> paras=new HashMap<>();
        JXDocument underTest = JXDocument.create(shopSignInInitResponse.body());
        List<JXNode>  nodes = underTest.selN("//script");
        String metaXml = nodes.get(nodes.size()-1).value().toString();
        String metaJson = metaXml.substring(metaXml.indexOf("{\"meta\":"),metaXml.indexOf("</script>"));
        JSON meta = JSONUtil.parse(metaJson);
        paras.put("serviceKey",meta.getByPath("signIn.customerLoginIDMS.d.serviceKey",String.class));

        paras.put("x_aos_model_page",meta.getByPath("meta.h.x-aos-model-page",String.class));
        paras.put("x_aos_stk",meta.getByPath("meta.h.x-aos-stk",String.class));
        paras.put("modelVersion",meta.getByPath("meta.h.modelVersion",String.class));
        paras.put("syntax",meta.getByPath("meta.h.syntax",String.class));
        paras.put("callbackSignInUrl",meta.getByPath("signIn.customerLoginIDMS.d.callbackSignInUrl",String.class));

        List<String> asSfaList =StrUtils.AdvancedCookieExtractor.getAnonymousCookieScripts(shopSignInInitResponse.body());
        // 匹配 as_sfa=后面的值直到分号
        String as_sfa = "";
        Pattern pattern = Pattern.compile("as_sfa=([^;]+);");
        Matcher matcher = pattern.matcher(asSfaList.get(0));
        if (matcher.find()) {
            as_sfa= matcher.group(1).trim();
        }
        paras.put("as_sfa",as_sfa);
        return paras;
    }

    /**
     * https://idmsa.apple.com/appleauth/auth/authorize/signin?frame_id=auth-03zmbfjx-nit8-yzr5-kund-j3iwbnh5&language=en_US&skVersion=7&iframeId=auth-03zmbfjx-nit8-yzr5-kund-j3iwbnh5&client_id=a797929d224abb1cc663bb187bbcd02f7172ca3a84df470380522a7c6092118b&redirect_uri=https://secure7.store.apple.com&response_type=code&response_mode=web_message&state=auth-03zmbfjx-nit8-yzr5-kund-j3iwbnh5&authVersion=latest
     * @return
     */
    public static HttpResponse authorizeSignin(HttpResponse initBalanceWithTunesResponse,Map<String,Object> paras) throws InterruptedException {
        // 每一步都检测中断信号
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        HashMap<String, List<String>> headers = new HashMap<>();
        String locationUrl=initBalanceWithTunesResponse.header("Location");
        String frameId= WebParasUtil.createFrameId();
        String locationBase= locationUrl.substring(0, locationUrl.indexOf("shop")-1);

        String clientId= MapUtil.getStr(paras,"serviceKey");

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("User-Agent", ListUtil.toList(BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Referer", ListUtil.toList(locationBase));
        headers.put("Upgrade-Insecure-Requests", ListUtil.toList("1"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("iframe"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-site"));
        headers.put("Priority", ListUtil.toList("u=4"));
        String url = "https://idmsa.apple.com/appleauth/auth/authorize/signin?frame_id="+frameId+"&language=en_US&skVersion=7&iframeId="+frameId
                +"&client_id="+clientId+"&redirect_uri="+locationBase+"&response_type=code&response_mode=web_message" +
                "&state="+frameId+"&authVersion=latest";
        String cookiesStr=CookieUtils.getCookiesFromHeader(initBalanceWithTunesResponse)
                +";as_sfa="+MapUtil.getStr(paras,"as_sfa")
                +";as_pcts="+MapUtil.getStr(paras,"as_pcts_cookies");
        HttpResponse signFrameResponse = ProxyUtil.execute(HttpUtil.createGet(url)
                        .cookie(cookiesStr)
                        .header(headers));

        paras.put("as_sfa",MapUtil.getStr(paras,"as_sfa"));
        paras.put("frameId",frameId);
        paras.put("clientId",clientId);
        paras.put("X-Apple-Auth-Attributes",signFrameResponse.header("X-Apple-Auth-Attributes"));
        paras.put("X-Apple-ID-Session-Id",signFrameResponse.header("X-Apple-ID-Session-Id"));
        paras.put("aasp",CookieUtils.getSomeCookieFromHeader(signFrameResponse,"aasp"));
        int xAppleHcBits = Integer.parseInt(signFrameResponse.header("X-Apple-HC-Bits"));
        paras.put("xAppleHcBits",xAppleHcBits);
        String xAppleHcChallenge = signFrameResponse.header("X-Apple-HC-Challenge");
        paras.put("xAppleHcChallenge",xAppleHcChallenge);
        Map<String,Object> result = WebParasUtil.calAWithout();
        String a= MapUtil.getStr(result,"a");
        BigInteger bigInt= (BigInteger) result.get("n");
        BigInteger ra= (BigInteger) result.get("ra");

        paras.put("a",a);
        paras.put("n",bigInt);
        paras.put("ra",ra);
        return signFrameResponse;
    }

    /**
     * 获取shldBtCk参数
     * @return
     */
    public static HttpResponse shldBtCkGenerator(HttpResponse initBalanceResponse, HttpResponse initBalanceWithTunesResponse,
                                                 HttpResponse shopSignInInitResponse,String requestBody,
                                                 String requestType,Map<String,Object> paras) throws InterruptedException {
        // 每一步都检测中断信号
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        HashMap<String, List<String>> headers = new HashMap<>();
        String location=initBalanceWithTunesResponse.header("Location");
        String locationBase=location.substring(0,location.indexOf("shop")-1);
        headers.put("Host", Arrays.asList(StrUtils.getHostFromUrl(locationBase)));
        headers.put("User-Agent", Arrays.asList(BROWSER_USER_AGENT));
        headers.put("Accept", Arrays.asList("*/*"));
        headers.put("Accept-Language", Arrays.asList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", Arrays.asList("gzip, deflate, br, zstd"));
        headers.put("Referer", Arrays.asList(location));
        headers.put("Connection", Arrays.asList("keep-alive"));
        headers.put("Sec-Fetch-Dest", Arrays.asList("empty"));
        headers.put("Sec-Fetch-Mode", Arrays.asList("cors"));
        headers.put("Sec-Fetch-Site", Arrays.asList("same-origin"));
        headers.put("Priority", Arrays.asList("u=4"));
        String url = locationBase+"/shop/shld/work/v1/q?wd=0";
        Map<String,String> cookiesMap=new HashMap<>();
        CookieUtils.setCookiesToMap(shopSignInInitResponse,cookiesMap);
        cookiesMap.put("as_pcts", CookieUtils.getSomeCookieFromHeader(initBalanceResponse,"as_pcts"));
        cookiesMap.put("as_sfa", MapUtil.getStr(paras,"as_sfa"));
        cookiesMap.put("pxro", "1");
        String cookies= MapUtil.join(cookiesMap,";","=",true);
        HttpResponse res ;
        if(requestType.equals("post")){
            cookies=cookies+";as_rumid="+MapUtil.getStr(paras,"as_rumid");
            res=ProxyUtil.execute(HttpUtil.createPost(url)
                    .cookie(cookies)
                    .body(requestBody)
                    .header(headers));

            paras.put("shld_bt_ck",CookieUtils.getSomeCookieFromHeader(res,"shld_bt_ck"));
        }else{
            res=ProxyUtil.execute(HttpUtil.createGet(url)
                    .cookie(cookies)
                    .header(headers));
        }
        paras.put("cookiesStr",cookies);
        return res;

    }

    /**
     * https://idmsa.apple.com/appleauth/auth/verify/device/key/challenge
     * @return
     */
    public static HttpResponse challenge(Map<String,Object> paras) throws InterruptedException {
        // 每一步都检测中断信号
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        String frameId= MapUtil.getStr(paras,"frameId");
        String clientId= MapUtil.getStr(paras,"clientId");
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("User-Agent", ListUtil.toList(BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info", ListUtil.toList(BROWSER_CLIENT_INFO));
        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(MapUtil.getStr( paras,"X-Apple-Auth-Attributes","")));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Client-Id", ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-State", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Redirect-URI", ListUtil.toList(MapUtil.getStr(paras,"locationBase")));
        headers.put("X-Apple-OAuth-Response-Type", ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode", ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type", ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-Domain-Id", ListUtil.toList("39"));
        headers.put("X-Apple-Locale", ListUtil.toList("CN-ZH"));
        headers.put("X-Requested-With", ListUtil.toList("XMLHttpRequest"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));

        Map<String,Object> bodyParas=new HashMap<>(){{
            put("passkeyAutofill",false);
        }};
        Map<String,Object> cookiesMap=new HashMap<>();
        cookiesMap.put("aasp", MapUtil.getStr(paras,"aasp"));


        String cookies=MapUtil.getStr(paras,"cookiesStr");
        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/verify/device/key/challenge")
                .header(headers)
                .body(JSONUtil.toJsonStr(bodyParas))
                .cookie(cookies));
        paras.put("scnt",res.header("scnt"));

        paras.put("cookiesStr",cookies);
        return res;
    }

    public static HttpResponse jslog(Map<String,Object> paras){
        String frameId= MapUtil.getStr(paras,"frameId");
        String clientId= MapUtil.getStr(paras,"clientId");
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("User-Agent", ListUtil.toList("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0"));
        headers.put("Accept", ListUtil.toList("application/json"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Scnt", ListUtil.toList(MapUtil.getStr(paras,"scnt")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info", ListUtil.toList(BROWSER_CLIENT_INFO));
        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(MapUtil.getStr(paras, "X-Apple-ID-Session-Id")));
        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(MapUtil.getStr(paras, "X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Client-Id", ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-State", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Redirect-URI", ListUtil.toList(MapUtil.getStr(paras,"locationBase")));
        headers.put("X-Apple-OAuth-Response-Type", ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode", ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type", ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-Domain-Id", ListUtil.toList("39"));
        headers.put("X-Apple-Locale", ListUtil.toList("CN-ZH"));
        headers.put("X-Requested-With", ListUtil.toList("XMLHttpRequest"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Te", ListUtil.toList("trailers"));

        String body = "{\"title\":\"Hashcash generation\",\"type\":\"INFO\",\"message\":\"APPLE ID : Performace - 0.006 s\",\"details\":\"{\\\"pageVisibilityState\\\":\\\"visible\\\"}\"}";
        Map<String,Object> cookiesMap = new HashMap<>();
        cookiesMap.put("shld_bt_ck", MapUtil.getStr(paras,"shld_bt_ck"));
        String cookiesStr = MapUtil.getStr(paras,"cookiesStr")+";"+MapUtil.join(cookiesMap,";","=",true);;
        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/jslog")
                .header(headers)
                .cookie(cookiesStr)
                .body(body));
        return res;
    }

    /**
     * https://idmsa.apple.com/appleauth/auth/federate?isRememberMeEnabled=true
     * @return
     */
    public static HttpResponse authFederate(String account,Map<String,Object> paras) throws InterruptedException {
        // 每一步都检测中断信号
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        String frameId= MapUtil.getStr(paras,"frameId");
        String clientId= MapUtil.getStr(paras,"clientId");
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("User-Agent", ListUtil.toList(BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Scnt", ListUtil.toList(MapUtil.getStr(paras,"scnt")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info", ListUtil.toList(BROWSER_CLIENT_INFO));
        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(MapUtil.getStr(paras,"X-Apple-ID-Session-Id")));
        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(MapUtil.getStr(paras,"X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Client-Id", ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-State", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Redirect-URI", ListUtil.toList(MapUtil.getStr(paras,"locationBase")));
        headers.put("X-Apple-OAuth-Response-Type", ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode", ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type", ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-Domain-Id", ListUtil.toList("39"));
        headers.put("X-Apple-Locale", ListUtil.toList("CN-ZH"));
        headers.put("X-Requested-With", ListUtil.toList("XMLHttpRequest"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));

        String body = "{\"accountName\":\""+account+"\",\"rememberMe\":false}";
        Map<String,Object> cookiesMap = new HashMap<>();
        cookiesMap.put("aa", MapUtil.getStr(paras,"aa_cookies"));
        cookiesMap.put("shld_bt_ck", MapUtil.getStr(paras,"shld_bt_ck"));
        String cookiesStr = MapUtil.getStr(paras,"cookiesStr")+";"+MapUtil.join(cookiesMap,";","=",true);;
        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/federate?isRememberMeEnabled=true")
                        .header(headers)
                        .cookie(cookiesStr)
                        .body(body));
        paras.put("cookiesStr",cookiesStr);
        return res;
    }
    /**
     * https://idmsa.apple.com/appleauth/auth/signin/init
     * @return
     */
    public static HttpResponse authSigninInit(String account,HttpResponse authFederateResponse,Map<String,Object> paras) throws InterruptedException {
        // 每一步都检测中断信号
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        String frameId= MapUtil.getStr(paras,"frameId");
        String clientId= MapUtil.getStr(paras,"clientId");
        String a= MapUtil.getStr(paras,"a");
        String locationBase= MapUtil.getStr(paras,"locationBase");
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("User-Agent", ListUtil.toList(BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Scnt", ListUtil.toList(authFederateResponse.header("scnt")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info", ListUtil.toList(BROWSER_CLIENT_INFO));
        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(MapUtil.getStr(paras,"X-Apple-ID-Session-Id")));
        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(MapUtil.getStr(paras,"X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Client-Id", ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-State", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Redirect-URI", ListUtil.toList(locationBase));
        headers.put("X-Apple-OAuth-Response-Type", ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode", ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type", ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-Domain-Id", ListUtil.toList("39"));
        headers.put("X-Apple-Locale", ListUtil.toList("CN-ZH"));
        headers.put("X-Requested-With", ListUtil.toList("XMLHttpRequest"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        String cookiesStr= MapUtil.getStr(paras,"cookiesStr")+";as_pcts="+MapUtil.getStr(paras,"as_pcts_cookies");
        String body = "{\"a\":\""+a+"\",\"accountName\":\""+account+"\",\"protocols\":[\"s2k\",\"s2k_fo\"]}";
        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/init")
                        .header(headers)
                        .cookie(cookiesStr)
                        .body(body));
        return res;
    }
    /**
     * https://idmsa.apple.com/appleauth/auth/signin/complete?isRememberMeEnabled=true
     * @return
     */
    public static HttpResponse signinCompete(String account,String pwd,HttpResponse authSigninInitResponse,Map<String,Object> paras) throws InterruptedException {
        // 每一步都检测中断信号
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        String frameId= MapUtil.getStr(paras,"frameId");
        String clientId= MapUtil.getStr(paras,"clientId");
        String locationBase= MapUtil.getStr(paras,"locationBase");
        String a= MapUtil.getStr(paras,"a");
        int xAppleHcBits= MapUtil.getInt(paras,"xAppleHcBits");
        String xAppleHcChallenge= MapUtil.getStr(paras,"xAppleHcChallenge");
        BigInteger n= MapUtil.get(paras,"n",BigInteger.class);
        BigInteger ra= MapUtil.get(paras,"ra",BigInteger.class);

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("User-Agent", ListUtil.toList(BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("X-APPLE-HC", ListUtil.toList(WebParasUtil.generateXAppleHC(xAppleHcBits,xAppleHcChallenge)));
        headers.put("Scnt", ListUtil.toList(authSigninInitResponse.header("scnt")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info", ListUtil.toList(BROWSER_CLIENT_INFO));
        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(MapUtil.getStr(paras,"X-Apple-ID-Session-Id")));
        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(MapUtil.getStr(paras,"X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Client-Id", ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-State", ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Redirect-URI", ListUtil.toList(locationBase));
        headers.put("X-Apple-OAuth-Response-Type", ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode", ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type", ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-Domain-Id", ListUtil.toList("39"));
        headers.put("X-Apple-Locale", ListUtil.toList("CN-ZH"));
        headers.put("X-Requested-With", ListUtil.toList("XMLHttpRequest"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));

        JSON json =  JSONUtil.parse(authSigninInitResponse.body());
        int iteration = json.getByPath("iteration",Integer.class);
        String salt = json.getByPath("salt",String.class);
        String b = json.getByPath("b",String.class);
        String c = json.getByPath("c",String.class);

        Map map = WebParasUtil.calM(account, pwd, a, iteration, salt, b, BigInteger.TWO, n, ra);
        Map<String,Object> bodyParas=new HashMap<>(){{
            put("accountName",account);
            put("rememberMe",false);
            put("m1",map.get("m1"));
            put("c",c);
            put("m2",map.get("m2"));
        }};
        Map<String,String> cookiesMap=new HashMap<>();
        cookiesMap.put("dslang",CookieUtils.getSomeCookieFromHeader(authSigninInitResponse,"dslang"));
        cookiesMap.put("site",CookieUtils.getSomeCookieFromHeader(authSigninInitResponse,"site"));
        String cookies= MapUtil.join(cookiesMap,";","=",true);
        String cookiesStr=MapUtil.getStr(paras,"cookiesStr")+";"+cookies;
        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/complete?isRememberMeEnabled=true")
                .header(headers)
                .body(JSONUtil.toJsonStr(bodyParas))
                .cookie(cookiesStr));
        return res;
    }


    /**
     *https://secure6.store.apple.com/shop/signIn/idms/authx?ssi=4AAABmnHR9-sBIKsyH7dLHr-6MdcIWm7US_EkmAtYVLCMw-izrcKzfZGCAAAAOGh0dHBzOi8vc2VjdXJlNi5zdG9yZS5hcHBsZS5jb20vc2hvcC9naWZ0Y2FyZC9iYWxhbmNlfHx8AAIBV1tgtBiyvAJiP7Bhu8MCv1F1HYWNI6GYsHpXPl_lAqg
     * @return
     */
    public static HttpResponse idmsAuthx(HttpResponse initBalanceResponse,HttpResponse signinCompeteResponse,Map<String,Object> paras) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        String syntax= MapUtil.getStr(paras,"syntax");
        String modelVersion= MapUtil.getStr(paras,"modelVersion");
        String x_aos_stk= MapUtil.getStr(paras,"x_aos_stk");
        String locationBase= MapUtil.getStr(paras,"locationBase");
        String location= MapUtil.getStr(paras,"location");
        String x_aos_model_page= MapUtil.getStr(paras,"x_aos_model_page");
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("Origin", ListUtil.toList(locationBase));
        headers.put("Referer", ListUtil.toList(location));

        headers.put("x-aos-model-page", ListUtil.toList(x_aos_model_page));
        headers.put("x-aos-stk",ListUtil.toList(x_aos_stk));
        headers.put("modelVersion",ListUtil.toList(modelVersion));
        headers.put("syntax",ListUtil.toList(syntax));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        headers.put("Sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("Sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("Sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("te",ListUtil.toList("trailers"));

        headers.put("User-Agent",ListUtil.toList(BROWSER_USER_AGENT));

        Map<String,String> cookiesMap=new HashMap<>();
        CookieUtils.setCookiesToMap(initBalanceResponse,cookiesMap);
        CookieUtils.setCookiesToMap(signinCompeteResponse,cookiesMap);
        cookiesMap.put("shld_bt_ck",MapUtil.getStr(paras,"shld_bt_ck"));
        cookiesMap.put("as_sfa",MapUtil.getStr(paras,"as_sfa"));
        String cookies= MapUtil.join(cookiesMap,";","=",true);

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("grantCode","");
        HttpResponse response = ProxyUtil.execute(HttpUtil.createPost(MapUtil.getStr(paras,"callbackSignInUrl"))
                        .header(headers)
                        .form(paramMap)
                        .cookie(cookies));
        CookieUtils.setCookiesToMap(response,cookiesMap);
        cookiesMap.remove("myacinfo");
        paras.put("cookies",MapUtil.join(cookiesMap,";","=",true));
        return response;
    }

    /**
     * https://secure6.store.apple.com/shop/giftcard/balance
     * @return
     */
    public static HttpResponse checkBalanceGet(HttpResponse idmsAuthxResponse ,String referer,String locationBase ) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br, zstd"));
        headers.put("Upgrade-Insecure-Requests", ListUtil.toList("1"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("document"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("navigate"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("none"));
        headers.put("Sec-Fetch-User", ListUtil.toList("?1"));
        headers.put("Referer", ListUtil.toList(referer));
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(locationBase+"/shop/giftcard/balance")
                .header(headers)
                .cookie(CookieUtils.getCookiesFromHeader(idmsAuthxResponse)));

        return res;
    }
    /**
     * https://secure7.store.apple.com/shop/giftcard/balancex?_a=checkBalance&_m=giftCardBalanceCheck
     * @return
     */
    public static HttpResponse checkBalance( Map<String, Object> cookiesMap,String giftCardPin) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("登录被中断");
        }
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("referer",ListUtil.toList(cookiesMap.get("locationBase")+ "/shop/giftcard/balance"));
        headers.put("origin",ListUtil.toList(MapUtil.getStr(cookiesMap,"locationBase")));

        headers.put("User-Agent",ListUtil.toList(BROWSER_USER_AGENT));

        headers.put("X-aos-model-page", ListUtil.toList("giftCardBalancePage"));
        headers.put("X-aos-stk",ListUtil.toList(MapUtil.getStr(cookiesMap,"x_aos_stk")));
        headers.put("modelVersion",ListUtil.toList(MapUtil.getStr(cookiesMap,"modelVersion")));
        headers.put("syntax",ListUtil.toList(MapUtil.getStr(cookiesMap,"syntax")));

        headers.put("X-requested-with",ListUtil.toList("Fetch"));

        headers.put("Sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("Sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("Sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("X-As-Actk",ListUtil.toList(MapUtil.getStr(cookiesMap,"x-as-actk")));
        Map<String,Object> data = new HashMap<>();
        data.put("giftCardBalanceCheck.giftCardPin",giftCardPin);
        data.put("giftCardBalanceCheck.deviceID", "%7B%22op%22%3A%22DEVICEID%22%7D");
        String url=MapUtil.getStr(cookiesMap,"locationBase")+ "/shop/giftcard/balancex?_a=checkBalance&_m=giftCardBalanceCheck";
        return ProxyUtil.execute(HttpUtil.createPost(url)
                .header(headers)
                .cookie(MapUtil.getStr(cookiesMap,"cookies"))
                .form(data));
    }
}





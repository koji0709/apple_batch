package com.sgswit.fx.utils.web;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.exception.UnavailableException;
import com.sgswit.fx.utils.CookieUtils;
import com.sgswit.fx.utils.StrUtils;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import com.sun.net.httpserver.HttpPrincipal;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.DigestFactory;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiftCardUtil {
    private static String BROWSER_USER_AGENT="Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0";
    private static String BROWSER_CLIENT_INFO="{\"U\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Nla44j1e3NlY5BNlY5BSmHACVZXnNA9.jMhquxilVVjpidPNs0oje9zH_y37lYAU.6elV2pNK1cllNIZ_wc6uTfwjNNlY5BNp55BNlan0Os5Apw.5ji\"}";
    private static String deviceID="%7B%22op%22%3A%22DEVICEID%22%7D";

    public static void main(String[] args) {

        String accountName="CurtisFinn779@gmail.com";
        String password="Juanjuan0414";
//        String accountName="pinbinJ2157@iCloud.com";
//        String password="Tiantian0402m.";
//        String accountName="djli0506@163.com";
//        String password="Gain280926";


        Map<String,Object> authParas=new HashMap<>();
        HttpResponse initBalanceResponse=initBalance("");
        authParas.put("as_pcts_cookies",CookieUtils.getSomeCookieFromHeader(initBalanceResponse,"as_pcts"));
        HttpResponse initBalanceWithTunesResponse=initBalanceWithTunes(initBalanceResponse);
        String location=initBalanceWithTunesResponse.header("Location");
        String locationBase=location.substring(0,location.indexOf("shop")-1);
        authParas.put("locationBase",locationBase);
        authParas.put("as_rumid",rumIdGenerator());

        HttpResponse shopSignInInitResponse=shopSignInInit(initBalanceWithTunesResponse,authParas);
        HttpResponse authorizeSigninResponse=authorizeSignin(initBalanceWithTunesResponse,shopSignInInitResponse,authParas);
        HttpResponse shldBtCkGeneratorGetResponse=shldBtCkGenerator(initBalanceResponse,initBalanceWithTunesResponse,shopSignInInitResponse,null,"get",authParas);
        JSONObject shldBtJsonObject=JSONUtil.parseObj(shldBtCkGeneratorGetResponse.body());
        // 添加 flagskv 对象
        JSONObject flagskv = new JSONObject();
        flagskv.set("patSkip", true);
        shldBtJsonObject.set("flagskv", flagskv);
        Map<String,Object> solvePoWMap=PoWSolver.solvePoW(shldBtJsonObject.getInt("low"),shldBtJsonObject.getInt("high")
                ,shldBtJsonObject.getInt("parts"),shldBtJsonObject.getBigInteger("result"),shldBtJsonObject.getLong("timeout"));
        // 添加 number 和 took
        shldBtJsonObject.set("number", solvePoWMap.get("numbers"));
        shldBtJsonObject.set("took", solvePoWMap.get("took"));
        HttpResponse shldBtCkGeneratorPostResponse=shldBtCkGenerator(initBalanceResponse,initBalanceWithTunesResponse,shopSignInInitResponse,shldBtJsonObject.toStringPretty(),"post",authParas);
        authParas.put("shld_bt_ck",CookieUtils.getSomeCookieFromHeader(shldBtCkGeneratorPostResponse,"shld_bt_ck"));

        HttpResponse challengeResponse=challenge(authParas);

        HttpResponse authFederateResponse=authFederate(accountName,authParas);
        HttpResponse authSigninInitResponse=authSigninInit(accountName,authFederateResponse,authParas);

        String as_pcts_cookies=CookieUtils.getSomeCookieFromHeader(initBalanceResponse,"as_pcts");
        authParas.put("as_pcts_cookies",as_pcts_cookies);
        HttpResponse signinCompeteResponse=signinCompete(accountName,password,authSigninInitResponse,authParas);

    }


    /**
     * 加载初始化地址 https://secure.store.apple.com/shop/giftcard/balance
     * @param countryCode
     * @return
     */
    public static HttpResponse initBalance(String countryCode){
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
        headers.put("Priority", ListUtil.toList("u=0"));
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
     * @param initBalanceResponse
     * @return
     */
    public static HttpResponse initBalanceWithTunes(HttpResponse initBalanceResponse)  {
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
     * @param initBalanceWitTunesRes
     * @return
     */
    public static HttpResponse shopSignInInit(HttpResponse initBalanceWitTunesRes,Map<String,Object> authParas){
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
        String serviceKey =meta.getByPath("signIn.customerLoginIDMS.d.serviceKey",String.class);
        paras.put("serviceKey",serviceKey);

        List<String> as_sfa_list=StrUtils.AdvancedCookieExtractor.getAnonymousCookieScripts(shopSignInInitResponse.body());
        // 匹配 as_sfa=后面的值直到分号
        String as_sfa = "";
        Pattern pattern = Pattern.compile("as_sfa=([^;]+);");
        Matcher matcher = pattern.matcher(as_sfa_list.get(0));

        if (matcher.find()) {
            as_sfa= matcher.group(1).trim();
        }
        paras.put("as_sfa",as_sfa);
        return paras;
    }

    /**
     * https://idmsa.apple.com/appleauth/auth/authorize/signin?frame_id=auth-03zmbfjx-nit8-yzr5-kund-j3iwbnh5&language=en_US&skVersion=7&iframeId=auth-03zmbfjx-nit8-yzr5-kund-j3iwbnh5&client_id=a797929d224abb1cc663bb187bbcd02f7172ca3a84df470380522a7c6092118b&redirect_uri=https://secure7.store.apple.com&response_type=code&response_mode=web_message&state=auth-03zmbfjx-nit8-yzr5-kund-j3iwbnh5&authVersion=latest
     * @param paras
     * @return
     */
    private static HttpResponse authorizeSignin(HttpResponse initBalanceWithTunesResponse, HttpResponse shopSignInInitResponse,Map<String,Object> paras){
        HashMap<String, List<String>> headers = new HashMap<>();
        String locationUrl=initBalanceWithTunesResponse.header("Location");
        String frameId= createFrameId();
        String locationBase= locationUrl.substring(0, locationUrl.indexOf("shop")-1);
        Map<String,Object> shopSignInInitResponseDocumentMap=parseShopSignInInitResponseDocument(shopSignInInitResponse);

        String clientId= MapUtil.getStr(shopSignInInitResponseDocumentMap,"serviceKey");

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
                +";as_sfa="+MapUtil.getStr(shopSignInInitResponseDocumentMap,"as_sfa")
                +";as_pcts="+MapUtil.getStr(paras,"as_pcts_cookies");
        HttpResponse signFrameResponse = ProxyUtil.execute(HttpUtil.createGet(url)
                        .cookie(cookiesStr)
                        .header(headers));

        paras.put("as_sfa",MapUtil.getStr(shopSignInInitResponseDocumentMap,"as_sfa"));
        paras.put("frameId",frameId);
        paras.put("clientId",clientId);
        paras.put("X-Apple-Auth-Attributes",signFrameResponse.header("X-Apple-Auth-Attributes"));
        paras.put("X-Apple-ID-Session-Id",signFrameResponse.header("X-Apple-ID-Session-Id"));
        paras.put("aasp",CookieUtils.getSomeCookieFromHeader(signFrameResponse,"aasp"));
        int xAppleHcBits = Integer.parseInt(signFrameResponse.header("X-Apple-HC-Bits"));
        paras.put("xAppleHcBits",xAppleHcBits);
        String xAppleHcChallenge = signFrameResponse.header("X-Apple-HC-Challenge");
        paras.put("xAppleHcChallenge",xAppleHcChallenge);
        //step1  signin
        String nHex = "AC6BDB41324A9A9BF166DE5E1389582FAF72B6651987EE07FC3192943DB56050A37329CBB4A099ED8193E0757767A13DD52312AB4B03310DCD7F48A9DA04FD50E8083969EDB767B0CF6095179A163AB3661A05FBD5FAAAE82918A9962F0B93B855F97993EC975EEAA80D740ADBF4FF747359D041D5C33EA71D281E446B14773BCA97B43A23FB801676BD207A436C6481F1D2B9078717461A5B9D32E688F87748544523B524B0D57D5EA77A2775D2ECFA032CFBDBF52FB3786160279004E57AE6AF874E7303CE53299CCC041C7BC308D82A5698F3A8D0C38271AE35F8E9DBFBB694B5C803D89F7AE435DE236D525F54759B65E372FCD68EF20FA7111F9E4AFF73";
        // N - 大素数模数
        BigInteger bigInt  = new BigInteger(nHex,16);
        // 使用 SecureRandom
        SecureRandom secureRandom = new SecureRandom();
        // a - 随机私钥
        byte[] rb = new byte[32];
        secureRandom.nextBytes(rb);
        // 确保正数
        BigInteger ra = new BigInteger(1,rb);
        String a = calA(ra,bigInt);
        paras.put("a",a);
        paras.put("n",bigInt);
        paras.put("ra",ra);
        return signFrameResponse;
    }

    /**
     * 获取shldBtCk参数
     * @param initBalanceResponse
     * @param initBalanceWithTunesResponse
     * @param shopSignInInitResponse
     * @param requestBody
     * @param requestType
     * @param paras
     * @return
     */
    public static HttpResponse shldBtCkGenerator(HttpResponse initBalanceResponse, HttpResponse initBalanceWithTunesResponse, HttpResponse shopSignInInitResponse,String requestBody,String requestType,Map<String,Object> paras){
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
     * @param paras
     * @return
     */
    public static HttpResponse challenge(Map<String,Object> paras){
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
        headers.put("X-Apple-Locale", ListUtil.toList("en_US"));
        headers.put("X-Requested-With", ListUtil.toList("XMLHttpRequest"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));

        Map<String,Object> bodyParas=new HashMap<>(){{
            put("passkeyAutofill",false);
        }};
        Map<String,Object> cookiesMap=new HashMap<>();
        cookiesMap.put("shld_bt_ck", MapUtil.getStr(paras,"shld_bt_ck"));
        cookiesMap.put("aasp", MapUtil.getStr(paras,"aasp"));


        String cookies=MapUtil.getStr(paras,"cookiesStr")+";"+MapUtil.join(cookiesMap,";","=",true);
        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/verify/device/key/challenge")
                .header(headers)
                .body(JSONUtil.toJsonStr(bodyParas))
                .cookie(cookies));
        paras.put("scnt",res.header("scnt"));

        paras.put("cookiesStr",cookies);
        return res;
    }

    /**
     * https://idmsa.apple.com/appleauth/auth/federate?isRememberMeEnabled=true
     * @param account
     * @param paras
     * @return
     */
    public static HttpResponse authFederate(String account,Map<String,Object> paras){
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
        headers.put("scnt", ListUtil.toList(MapUtil.getStr(paras,"scnt")));
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
        headers.put("X-Apple-Locale", ListUtil.toList("en_US"));
        headers.put("X-Requested-With", ListUtil.toList("XMLHttpRequest"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));

        String body = "{\"accountName\":\""+account+"\",\"rememberMe\":false}";

        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/federate?isRememberMeEnabled=true")
                        .header(headers)
                        .cookie(MapUtil.getStr(paras,"cookiesStr"))
                        .body(body));
        return res;
    }

    public static HttpResponse authSigninInit(String account,HttpResponse authFederateResponse,Map<String,Object> paras){
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
        headers.put("scnt", ListUtil.toList(authFederateResponse.header("scnt")));
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
        headers.put("X-Apple-Locale", ListUtil.toList("en_US"));
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

    public static HttpResponse signinCompete(String account,String pwd,HttpResponse authSigninInitResponse,Map<String,Object> paras){
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
        headers.put("X-APPLE-HC", ListUtil.toList(generateXAppleHC(xAppleHcBits,xAppleHcChallenge)));
        headers.put("scnt", ListUtil.toList(authSigninInitResponse.header("scnt")));
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
        headers.put("X-Apple-Locale", ListUtil.toList("en_US"));
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

        Map map = calM(account, pwd, a, iteration, salt, b, BigInteger.TWO, n, ra);
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



    public static HttpResponse shopSignin(HttpResponse step2Res,HttpResponse pre1,Map<String,Object> paras){
        String syntax= MapUtil.getStr(paras,"syntax");
        String modelVersion= MapUtil.getStr(paras,"modelVersion");
        String x_aos_stk= MapUtil.getStr(paras,"x_aos_stk");
        String locationBase= MapUtil.getStr(paras,"locationBase");
        String location= MapUtil.getStr(paras,"location");
        String x_aos_model_page= MapUtil.getStr(paras,"x_aos_model_page");
        String as_sfa_cookie= MapUtil.getStr(paras,"as_sfa_cookie");
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
        CookieUtils.setCookiesToMap(pre1,cookiesMap);
        CookieUtils.setCookiesToMap(step2Res,cookiesMap);
        CookieUtils.setCookiesToMap(pre1,cookiesMap);
        cookiesMap.put("shld_bt_ck",paras.get("shld_bt_ck").toString());
        String cookies= MapUtil.join(cookiesMap,";","=",true);
        cookies=cookies+";"+as_sfa_cookie;

        Map<String,Object> paramMap = new HashMap<>();

        paramMap.put("grantCode","");
        HttpResponse res3 = ProxyUtil.execute(HttpUtil.createPost(location.substring(0,location.indexOf("shop")) +
                        "shop/signIn/idms/authx" +
                        location.substring(location.indexOf("?")))
                        .header(headers)
                        .form(paramMap)
                        .cookie(cookies));
        return res3;
    }

    public static HttpResponse checkBalance( Map<String, Object> paras,String giftCardPin){
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("referer",ListUtil.toList(paras.get("locationBase")+ "shop/giftcard/balance"));
        headers.put("origin",ListUtil.toList(MapUtil.getStr(paras,"locationBase")));

        headers.put("User-Agent",ListUtil.toList(BROWSER_USER_AGENT));

        headers.put("x-aos-model-page", ListUtil.toList("giftCardBalancePage"));
        headers.put("x-aos-stk",ListUtil.toList(MapUtil.getStr(paras,"x_aos_stk")));
        headers.put("modelVersion",ListUtil.toList(MapUtil.getStr(paras,"modelVersion")));
        headers.put("syntax",ListUtil.toList(MapUtil.getStr(paras,"syntax")));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        headers.put("Sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("Sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("Sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("X-As-Actk",ListUtil.toList(MapUtil.getStr(paras,"x-as-actk")));
        Map<String,Object> data = new HashMap<>();
        data.put("giftCardBalanceCheck.giftCardPin",giftCardPin);
        data.put("giftCardBalanceCheck.deviceID",deviceID);
        String location=MapUtil.getStr(paras,"location");
        String url=location.substring(0,location.indexOf("shop")) + "shop/giftcard/balancex?_a=checkBalance&_m=giftCardBalanceCheck";
        HttpResponse res4 = ProxyUtil.execute(HttpUtil.createPost(url)
                .header(headers)
                .cookie(MapUtil.getStr(paras,"cookies"))
                .form(data));
        return res4;
    }



    protected static Map<String,String> calM(String accountName, String password, String a, Integer iter, String salt, String b, BigInteger g, BigInteger n, BigInteger ra) {
        // calculatek // k = h(n|g) 直接串联,并且按照位数对齐，不足的前面补0凑
        byte[] nb = n.toByteArray();
        byte[] gb = g.toByteArray();

        if(nb.length > 256){
            nb = ArrayUtil.remove(nb,0);
        }

        //SRPPassword 计算srp P 字段，
        byte[] p = SRPPassword(password, salt, iter);
        // calculateX // x = SHA(s | SHA(U | ":" | p))
        BigInteger X = calculateX(salt, p);
        BigInteger bigB = new BigInteger(1,Base64.decode(b));
        BigInteger bigA = new BigInteger(HexUtil.encodeHexStr(Base64.decode(a)),16);

        byte[] ab = bigA.toByteArray();
        byte[] bb = bigB.toByteArray();
        if(ab.length>256){
            ab = ArrayUtil.remove(ab,0);
        }
        if(bb.length>256){
            bb = ArrayUtil.remove(bb,0);
        }

        // calculateU // U = SHA(a | b)
        BigInteger u= calculateU(ab,bb);

        BigInteger k = calculatek(nb, gb);

        //calculateS
        BigInteger S = calculateS(k,X, ra,bigB,u, n, g);

        //calculateK
        byte[] K = calculateK(S);

        //calculateM1
        byte[] m1 = calculateM1(accountName, salt,ab,bb,K, nb, gb);
        //calculateM2
        byte[] m2 = calculateM2(bigA,m1,K);
        Map<String,String> map = new HashMap<>();
        map.put("m1",Base64.encode(m1));
        map.put("m2",Base64.encode(m2));
        return map;
    }

    private static byte[] SRPPassword(String password,String salt,int iter){

        try {
            String algorithm = "PBKDF2WithHmacSHA256";
            int keyLength = 256;

            Digester digester = new Digester(DigestAlgorithm.SHA256);
            byte[] p = digester.digest(password.getBytes());
            byte[] sb = Base64.decode(salt);
            PBEParametersGenerator generator = new PKCS5S2ParametersGenerator(DigestFactory.createSHA256());
            generator.init(p, sb, iter);
            KeyParameter params = (KeyParameter)generator.generateDerivedParameters(keyLength);
            byte[] key = params.getKey();
            return key;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] calculateM2(BigInteger bigA, byte[] m1, byte[] k){
        Digester digester = new Digester(DigestAlgorithm.SHA256);
        byte[] m2 = digester.digest(ArrayUtil.addAll(bigA.toByteArray(),m1,k));
        return m2;
    }

    private static byte[] calculateM1(String accountName , String salt,byte[] ab,byte[] bb,byte[] k,byte[] nb,byte[] gb){

        byte[] pp = new byte[255];
        for(int i = 0 ; i < 255; i ++){
            pp[i] = 0;
        }

        Digester digester1 = new Digester(DigestAlgorithm.SHA256);
        byte[] digestn = digester1.digest(nb);

        Digester digester2 = new Digester(DigestAlgorithm.SHA256);
        byte[] digestg = digester2.digest(ArrayUtil.addAll(pp,gb));

        Digester digester3 = new Digester(DigestAlgorithm.SHA256);
        byte[] digenti = digester3.digest(accountName);

        byte[] hxor = new byte[digestn.length];
        for(int i = 0; i < digestn.length; i++){
            hxor[i] = (byte)(digestn[i] ^ digestg[i]);
        }

        Digester digester4 = new Digester(DigestAlgorithm.SHA256);

        byte[] m1 = digester4.digest(ArrayUtil.addAll(hxor,digenti, Base64.decode(salt),ab,bb,k));

        return m1;
    }

    private static byte[] calculateK(BigInteger S){
        Digester digester = new Digester(DigestAlgorithm.SHA256);

        byte[] s = S.toByteArray();
        if(s.length > 256){
            s = ArrayUtil.remove(s,0);
        }
        byte[] d = digester.digest(s);
        return d;
    }

    /* Client Side S = (B - k*(g^x)) ^ (a + ux) */
    private static BigInteger calculateS(BigInteger k , BigInteger X , BigInteger a,BigInteger b,BigInteger u,BigInteger n, BigInteger g){

        BigInteger result1 = g.modPow(X,n);

        BigInteger result2 = k.multiply(result1);

        BigInteger result3 = b.subtract(result2);

        BigInteger result4 = u.multiply(X);

        BigInteger result5 = a.add(result4);

        BigInteger result6 = result3.modPow(result5,n);

        BigInteger result7 = result6.mod(n);

        return result7;
    }

    private static BigInteger calculatek(byte[] nb,byte[] gb){

        byte[] pp = new byte[255];
        for(int i = 0 ; i < 255; i ++){
            pp[i] = 0;
        }

        byte[] h = ArrayUtil.addAll(nb,pp,gb);
        Digester digester = new Digester(DigestAlgorithm.SHA256);
        byte[] d = digester.digest(h);

        BigInteger k = new BigInteger(1,d);
        return k;
    }

    private static BigInteger calculateU(byte[] ab,byte[] bb){
        Digester digester1 = new Digester(DigestAlgorithm.SHA256);

        byte[] a = ArrayUtil.addAll(ab,bb);
        byte[] d = digester1.digest(a);

        BigInteger u = new BigInteger(1,d);
        return u;
    }

    private static BigInteger calculateX(String salt, byte[] password){

        Digester digester1 = new Digester(DigestAlgorithm.SHA256);
        byte[] d1 = digester1.digest(ArrayUtil.addAll(":".getBytes(StandardCharsets.UTF_8),password));

        Digester digester2 = new Digester(DigestAlgorithm.SHA256);
        byte[] d2 = digester2.digest(ArrayUtil.addAll(Base64.decode(salt),d1));

        BigInteger x = new BigInteger(1,d2);

        return  x;
    }

    protected static String calA(BigInteger a,BigInteger n) {

        BigInteger g = new BigInteger("2");
        BigInteger A = g.modPow(a, n);

        // 更安全的字节数组处理
        byte[] aBytes = A.toByteArray();
        int expectedLength = 256; // 2048位 = 256字节

        if (aBytes.length > expectedLength && aBytes[0] == 0) {
            // 移除前导的符号位0
            aBytes = Arrays.copyOfRange(aBytes, 1, aBytes.length);
        } else if (aBytes.length < expectedLength) {
            // 补零到指定长度
            byte[] padded = new byte[expectedLength];
            System.arraycopy(aBytes, 0, padded, expectedLength - aBytes.length, aBytes.length);
            aBytes = padded;
        }
        return Base64.encode(aBytes);
    }

    private static String generateXAppleHC(int xAppleHcBits,String xAppleHcChallenge) {
        String version = "1";
        String date = DateUtil.format(new DateTime(TimeZone.getTimeZone("GMT")), "yyyyMMddHHmmss");

        // 初始 hc 基础部分
        String hcBase = version + ":" + xAppleHcBits + ":" +date+ ":" + xAppleHcChallenge + "::";

        int bytes = (int) Math.ceil(xAppleHcBits / 8.0);

        int counter = 0;
        boolean found = false;

        while (!found) {
            // 计算 SHA1(hcBase + counter)
            Digester digester = new Digester(DigestAlgorithm.SHA1);
            byte[] digest = digester.digest(hcBase + counter);

            // 取前 bytes 字节
            byte[] prefix = ArrayUtil.sub(digest, 0, bytes);

            // 转为二进制字符串
            StringBuilder bitStr = new StringBuilder();
            for (byte b : prefix) {
                bitStr.append(getBitString(b));
            }

            // 判断前 xAppleHcBits 位是否全为 0
            if (bitStr.substring(0, xAppleHcBits).equals("0".repeat(xAppleHcBits))) {
                found = true;
                break;
            }
            counter++;
        }

        // 拼接完整 X-Apple-HC
        return hcBase + counter;
    }

    private static String getBitString(byte b) {
        StringBuilder bits = new StringBuilder(Integer.toBinaryString(b & 0xFF));
        while (bits.length() < 8) bits.insert(0, '0'); // 补足8位
        return bits.toString();
    }




    private static String createFrameId(){

        Digester md5 = new Digester(DigestAlgorithm.MD5);
        StringBuilder sb = new StringBuilder();

        sb.append("auth-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,10));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,6));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,6));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,6));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,10));

        return sb.toString();
    }

    private static String rumIdGenerator(){
        return UUID.randomUUID().toString();
    }
}





package com.sgswit.fx.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.DigestFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author DELL
 */
public class WebLoginUtil {
    public static String createClientId(){
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        StringBuilder sb = new StringBuilder();
        sb.append("a");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(1));
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(1));
        sb.append("b");
        return sb.toString();
    }
    public static HttpResponse signin(Map<String,Object> signInMap){

        String frameId  = createFrameId();
        String clientId = MapUtil.getStr(signInMap,"serviceKey");

        //step1  signin
        String nHex = "AC6BDB41324A9A9BF166DE5E1389582FAF72B6651987EE07FC3192943DB56050A37329CBB4A099ED8193E0757767A13DD52312AB4B03310DCD7F48A9DA04FD50E8083969EDB767B0CF6095179A163AB3661A05FBD5FAAAE82918A9962F0B93B855F97993EC975EEAA80D740ADBF4FF747359D041D5C33EA71D281E446B14773BCA97B43A23FB801676BD207A436C6481F1D2B9078717461A5B9D32E688F87748544523B524B0D57D5EA77A2775D2ECFA032CFBDBF52FB3786160279004E57AE6AF874E7303CE53299CCC041C7BC308D82A5698F3A8D0C38271AE35F8E9DBFBB694B5C803D89F7AE435DE236D525F54759B65E372FCD68EF20FA7111F9E4AFF73";
        BigInteger n = new BigInteger(nHex,16);
        BigInteger g = new BigInteger("2");

        byte[] rb = RandomUtil.randomBytes(32);
        BigInteger ra = new BigInteger(1,rb);
        String a = calA(ra,n);

        signInMap.put("frameId",frameId);
        signInMap.put("clientId",clientId);

        HttpResponse step0Res = auth(signInMap);
        signInMap.put("a",a);
        HttpResponse step1Res = signinInit(step0Res,signInMap);
        if(step1Res.getStatus()==503){
            return step1Res;
        }
        signInMap.put("g",g);
        signInMap.put("n",n);
        signInMap.put("ra",ra);
        HttpResponse step2Res = signinCompete(step1Res,step0Res,signInMap);
        return  step2Res;
    }

    private static HttpResponse auth(Map<String,Object> signInMap){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type",ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        String redirectUri ="";
        if(!StringUtils.isEmpty(MapUtil.getStr(signInMap,"callbackSignInUrl"))){
            redirectUri=MapUtil.getStr(signInMap,"callbackSignInUrl");
            redirectUri = redirectUri.substring(0,redirectUri.indexOf("shop"));
        }else{
            redirectUri="https://www.apple.com/";
        }
        String frameId=MapUtil.getStr(signInMap,"frameId");
        String clientId=MapUtil.getStr(signInMap,"clientId");
        String url = "https://idmsa.apple.com/appleauth/auth/authorize/signin?frame_id="+frameId+"&skVersion=7" +
                "&iframeId="+frameId+"&client_id="+clientId+"&redirect_uri="+ redirectUri +"&response_type=code" +
                "&response_mode=web_message&state="+frameId+"&authVersion=latest";
        Map<String,String> cookiesMap=new HashMap<>();
        if(null==signInMap.get("cookiesMap")){
            cookiesMap=new HashMap<>();
        }else{
            cookiesMap= (Map<String, String>) signInMap.get("cookiesMap");
        }
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(url)
                        .cookie(cookiesMap.size()==0?"geo=CN;" :MapUtil.join(cookiesMap,";","=",true))
                        .header(headers));

        CookieUtils.setCookiesToMap(res,cookiesMap);

        signInMap.put("cookiesMap" , CookieUtils.setCookiesToMap(res,cookiesMap));
        return res;
    }

    private static HttpResponse signinInit(HttpResponse res1,Map<String,Object> paras){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("scnt",ListUtil.toList(res1.header("scnt")));
        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(res1.header("X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(MapUtil.getStr(paras,"clientId")));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Redirect-URI",ListUtil.toList("https://www.apple.com/"));
        headers.put("X-Apple-OAuth-Client-Id",ListUtil.toList(MapUtil.getStr(paras,"clientId")));
        headers.put("X-Apple-OAuth-Client-Type",ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(MapUtil.getStr(paras,"frameId")));
        headers.put("X-Apple-Domain-Id",ListUtil.toList("1"));
        headers.put("X-Apple-Frame-Id",ListUtil.toList(MapUtil.getStr(paras,"frameId")));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        String body = "{\"a\":\""+MapUtil.getStr(paras,"a")+"\",\"accountName\":\""+ MapUtil.getStr(paras,"account") +"\",\"protocols\":[\"s2k\",\"s2k_fo\"]}";
        HttpRequest httpRequest=HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/init")
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .body(body);
        HttpResponse res = ProxyUtil.execute(httpRequest);
        return res;
    }

    private static HttpResponse signinCompete(HttpResponse res1,HttpResponse res0,Map<String,Object> paras){
        String a=MapUtil.getStr(paras,"a");
        BigInteger g=new BigInteger(MapUtil.getStr(paras,"g"));
        BigInteger n=new BigInteger(MapUtil.getStr(paras,"n"));
        BigInteger ra=new BigInteger(MapUtil.getStr(paras,"ra"));
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("scnt",ListUtil.toList(res1.header("scnt")));
        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(res0.header("X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(MapUtil.getStr(paras,"clientId")));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Redirect-URI",ListUtil.toList("https://www.apple.com/"));
        headers.put("X-Apple-OAuth-Client-Id",ListUtil.toList(MapUtil.getStr(paras,"clientId")));
        headers.put("X-Apple-OAuth-Client-Type",ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(MapUtil.getStr(paras,"frameId")));
        headers.put("X-Apple-Domain-Id",ListUtil.toList("1"));
        headers.put("X-Apple-Frame-Id",ListUtil.toList(MapUtil.getStr(paras,"frameId")));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        int xAppleHcBits = Integer.parseInt(res0.header("X-Apple-HC-Bits"));
        String xAppleHcChallenge = res0.header("X-Apple-HC-Challenge");

        String hc = calCounter(xAppleHcBits,xAppleHcChallenge);

        headers.put("X-APPLE-HC",ListUtil.toList(hc));

        JSON json = JSONUtil.parse(res1.body());

        int iter = (Integer) json.getByPath("iteration");
        String salt = (String)json.getByPath("salt");
        String b = (String) json.getByPath("b");
        String c = (String)json.getByPath("c");

        Map map = calM(MapUtil.getStr(paras,"account"), MapUtil.getStr(paras,"pwd"), a, iter, salt, b, g, n, ra);
        Map<String,String> cookiesMap;
        if(null==paras.get("cookiesMap")){
            cookiesMap=new HashMap<>();
        }else{
            cookiesMap= (Map<String, String>) paras.get("cookiesMap");
        }
        cookiesMap.put("geo","CN");
        String body = "{\"accountName\":\""+MapUtil.getStr(paras,"account")+"\",\"rememberMe\":false,\"m1\":\""+ map.get("m1") +"\",\"c\":\""+ c +"\",\"m2\":\"" + map.get("m2") +"\"}";
        HttpRequest httpRequest=HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/complete?isRememberMeEnabled=true")
                .header(headers)
                .cookie(MapUtil.join(cookiesMap,";","=",true))
                .body(body);
        HttpResponse res = ProxyUtil.execute(httpRequest);


        paras.put("cookiesMap" , CookieUtils.setCookiesToMap(res,cookiesMap));
        paras.put("countryCode",res.header("X-Apple-ID-Account-Country"));

        return res;
    }

    private static String calCounter(int xAppleHcBits,String xAppleHcChallenge) {
        String version = "1";
        String date = DateUtil.format(new DateTime(TimeZone.getTimeZone("GMT")),"yyyyMMddHHmmss");

        String hc = version + ":" + xAppleHcBits + ":" + date + ":" + xAppleHcChallenge + "::";

        int bytes = (int) Math.ceil(xAppleHcBits / 8.0);

        int counter = 0;
        boolean isZero = false;
        while(!isZero){
            Digester digester = new Digester(DigestAlgorithm.SHA1);
            byte[] d = digester.digest(hc+counter);
            byte[] prefix = ArrayUtil.sub(d, 0, bytes);

            String bitStr = "";
            for (int i = 0; i < bytes; i++) {
                bitStr += getBit(prefix[i]);
            }

            String zeroStr = "";
            for(int k = 0; k < xAppleHcBits; k++){
                zeroStr += "0";
            }

            if(bitStr.substring(0, xAppleHcBits).equals(zeroStr)){
                isZero = true;
                break;
            }
            counter++;
        }
        return hc + counter;
    }

    private static String getBit(byte by){
        StringBuffer sb = new StringBuffer();
        sb.append((by>>7)&0x1)
                .append((by>>6)&0x1)
                .append((by>>5)&0x1)
                .append((by>>4)&0x1)
                .append((by>>3)&0x1)
                .append((by>>2)&0x1)
                .append((by>>1)&0x1)
                .append((by>>0)&0x1);
        return sb.toString();
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


    private static String calA(BigInteger a,BigInteger n) {

        BigInteger g = new BigInteger("2");
        BigInteger ai = g.modPow(a,n);

        byte[] aib = ai.toByteArray();
        if(aib.length > 256){
            aib = ArrayUtil.remove(aib,0);
        }
        String a2k = Base64.encode(aib);
        return  a2k;
    }

    private static Map<String,String> calM(String accountName, String password, String a, Integer iter, String salt, String b, BigInteger g, BigInteger n, BigInteger ra) {
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

}

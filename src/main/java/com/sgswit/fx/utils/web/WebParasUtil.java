package com.sgswit.fx.utils.web;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.DigestFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author DeZh
 * @date 2025/11/8 14:24
 * @description
 */
public class WebParasUtil {
    public static String createClientId(){
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        StringBuilder sb = new StringBuilder();
        sb.append("a");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(1));
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(1));
        sb.append("b");
        return sb.toString();
    }

    public static String generateXAppleHC(int xAppleHcBits, String xAppleHcChallenge) {
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
    public static Map<String,String> calM(String accountName, String password, String a, Integer iter, String salt, String b, BigInteger g, BigInteger n, BigInteger ra) {
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

    public static String calA(BigInteger ra, BigInteger n) {

        BigInteger g = new BigInteger("2");
        BigInteger aa = g.modPow(ra, n);

        // 更安全的字节数组处理
        byte[] aBytes = aa.toByteArray();
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


    public static Map<String,Object> calAWithout() {
        Map<String,Object> result = new HashMap<>();
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
        BigInteger g = new BigInteger("2");
        BigInteger aa = g.modPow(ra, bigInt);

        // 更安全的字节数组处理
        byte[] aBytes = aa.toByteArray();
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
        result.put("g",g);
        result.put("n",bigInt);
        result.put("a", Base64.encode(aBytes));
        result.put("ra", ra);
        return result;
    }

    public static String createFrameId(){

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

    public static String rumIdGenerator(){
        return UUID.randomUUID().toString();
    }
}

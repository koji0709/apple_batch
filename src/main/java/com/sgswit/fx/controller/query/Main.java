package com.sgswit.fx.controller.query;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.db.ActiveEntity;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.utils.AppleIDUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author yanggang
 * @createTime 2023/10/11
 */
public class Main {


    public static void main(String[] args) throws Exception {

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Connection", ListUtil.toList("keep-alive"));
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        String url = "https://idmsa.apple.com/appleauth/auth/signin?isRememberMeEnabled=true";

        String accountName = "gbkrccqrfbg@hotmail.com";
        String password = "Weiqi100287.";
        String body = "{\"accountName\":\""+accountName+"\",\"rememberMe\":true,\"password\":\""+password+"\"}";
        HttpResponse execute = HttpUtil.createPost(url)
                .body(body)
                .header(headers)
                .execute();
        String body1 = execute.body();
        if(body1.contains("serviceErrors")){
            System.out.println("账号或密码错误！");
        }

        HashMap<String, List<String>> headers1 = new HashMap<>();
//        headers1.put("Authority",ListUtil.toList("secure6.store.apple.com"));
//        headers1.put("Method",ListUtil.toList("GET"));
//        headers1.put("Path",ListUtil.toList("/shop/checkout/start?pltn=D20C80B9"));
//        headers1.put("Scheme",ListUtil.toList("https"));
        headers1.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"));
        headers1.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers1.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers1.put("Cookie",ListUtil.toList("dssf=1; dssid2=28ef90d7-13c8-4e63-8777-de4171652925; as_sfa=Mnx1c3x1c3x8ZW5fVVN8Y29uc3VtZXJ8aW50ZXJuZXR8MHwwfDE; pxro=1; as_dc=ucp3; geo=CN; s_cc=true; as_pcts=dR9efuO0rxG+JrDwuUV_ZhFdYdujHHu3+QO2jgIHXK0_D3C3KlmiKk:YeldCzQwPokQhNRDOZSrKdj0Yi4sIqrW6+S5sIfGYwCvCyti8DzlPZo8zQQ44__u7NdgEuylIYVGmgVFl0FyfaGMQ6-rvWiDXY8CviqplJp:; dslang=US-EN; site=USA; s_fid=6071F92805DA6017-3DB274C1941BB2F1; s_vi=[CS]v1|32B4C95DFC6C28A0-40001F1E80EA1D67[CE]; at_check=true; acn01=1XVzUzrDSQSeVsU07iBhf17M2gKh1FOY7CjkgTSadwAPLmULRJXK; as_cn=~WIL41BxfL_kpkBhz0jG5Q6Pt_eQDtx__cOH5jkpB2R8=; as_disa=AAAjAAABOPyaC8WQGrDtVwhNdWFj6p9798ea4cUALLK2kh827tYROoOSHHgyZGQp7BjY6ZFHAAIBktxlxsm6KndJdgwdftvDXT4ErRvkzOISaiqT8s0ixg8=; as_rec=b10749a69b71341d8624726ba12c866e32a4e73b6b3866a83b20d0f4f61820c6c38da3a8d11806fa6bf1cccdf9650b3759dbf9d682c97c943bf9225efca8b244af7a395f1d018c7311f93e6072871cf3; as_ltn_us=AAQEAMOqaoGyjLrTEozLeBzKnWIbsRneoriQNRtvxDD3P-xuQFiMarBxBVDj4ULggLnRjbxR2jUnN1NC8PQgqgm9o0ChGEMJq1w; s_sq=%5B%5BB%5D%5D; pt-dm=v1~x~ft4pet82~m~2~n~AOS%3A%20bag~r~aos:bag"));
        headers1.put("Sec-Ch-Ua",ListUtil.toList("\"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\""));
        headers1.put("Sec-Ch-Ua-Mobile",ListUtil.toList("?0"));
        headers1.put("Sec-Ch-Ua-Platform",ListUtil.toList("\"Windows\""));
        headers1.put("Sec-Fetch-Dest",ListUtil.toList("document"));
        headers1.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers1.put("Sec-Fetch-Site",ListUtil.toList("same-site"));
        headers1.put("Sec-Fetch-User",ListUtil.toList("?1"));
        headers1.put("Upgrade-Insecure-Requests",ListUtil.toList("1"));
        headers1.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"));
        headers1.put("Host", ListUtil.toList("https://www.apple.com/"));

        String url1 = "https://secure6.store.apple.com/shop/checkout/start?pltn=D20C80B9";
        HttpResponse execute1 = HttpUtil.createGet(url1)
                .header(headers)
                .execute();
        String location = execute1.header("Location");


        //解析数据
//        String s1 = a.getByPath("checkout.billing.billingOptions.d.options").toString();
//        if(s1.contains("has been disabled")){
//            //余额body
//            String num = "checkout.billing.billingOptions.selectedBillingOptions.appleBalance.appleBalanceInput.d.availableAppleBalance";
//            String num1 = (String) a.getByPath(num);
//            System.out.println(num1);
//        }else {
//            System.out.println("不为灰余额账户");
//        }



    }

}

package com.sgswit.fx.controller.query;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.db.ActiveEntity;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
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



//        String url = "data:image/jpeg;base64,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";
//        HttpResponse execute = HttpUtil.createGet(url).execute();
//        System.out.println(execute);
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Connection", ListUtil.toList("keep-alive"));
        headers.put("Host", ListUtil.toList("privacy.apple.com"));
        headers.put("Referer", ListUtil.toList("https://privacy.apple.com/"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("sec-ch-ua-platform", ListUtil.toList("\"Windows\""));

        headers.put("sec-ch-ua", ListUtil.toList("Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114"));
        headers.put("sec-ch-ua-mobile", ListUtil.toList("?0"));
        headers.put("cookie", ListUtil.toList("POD=cn~zh; XID=e9bd622e89e45f39cbf8472370e9db28; geo=CN; dslang=CN-ZH; site=CHN; _csrf=KY7k4cfDsag3r8qJtSExAein; itspod=29; caw-at=eyJraWQiOiIxIiwiYWxnIjoiRVMyNTYifQ.eyJzdWIiOiIwMDAzMDItMDgtODFlNjczMmYtYWE2Ny00NDU4LTk1NWItMzRhNGFhYzhiZGM1IiwiaXNzIjoiY29tLmFwcGxlLmlkbXMuY2xpZW50IiwiZXhwIjoxNjk3NTk1MTc5LCJpYXQiOjE2OTc1OTQyNzl9.jc-5hQmh0Wr__ABTk3OKpbM14mWKhpXVcDX2ErE6yxRLhjL54trZDEhc-4SdR4RvrMY6fXxrizTpYRqHjLXnCw; acn01=0d3TzaRts3tQFjvIb/ei5V0VKuXn0o0AAZxAjj9bag==; pgsession=46D7FEC0F4651DBB4FAFD6C148BD1A0CDD3AF9B0DEEE20AC054A4C4563F9EB48BABDEE9375FD76D3F6C761E78924DC337CB374BEDC1D8D0DFB853D66228B9012C2BA4414CC75B3D26D9F6C551EE6C92C0EE89284D260AA3E66E9B24806F79E2D8CEFD3D2BDEF60558190E6D5161B5E1DC833F109564C4A51; pgtoken=AAAAODN8MDAwMzAyLTA4LTgxZTY3MzJmLWFhNjctNDQ1OC05NTViLTM0YTRhYWM4YmRjNQAAAYtAmQU%2BdbgxIpK3Asl79NSnKFdL7w5mZqWuSP5JUyLFym5GJosDW1uSM9NLiFrnSIG6YeQI3bV4OHAT%2F2pc1guqOGjwk5Hqu%2BLRx77ZnGLx2O2eFEIct2xfNSXicdwugziCrVSl%2FPq9vxpzMWrseLN%2BLYMlkVNsxMYR8H6COfbaChmDTsJmJUVrHwRkUzO%2BkFlRlGtKcZB4H2xArcpilNr1Gdyj2iiEWqEgItR0vrN9fc9%2BRNIMJGM%2BAWPV9aQRv2xU8uv2e2H6V6nxPnyWK4UzKR72%2B%2FCmMrqJqD%2B6xWYUU2Nmj5lkh%2FQrSLjcqSTQX2vwDmyFasQWz2OMCOiRIw73X5qUJjzDk3Kd%2F5Z93FHndjuFRuvMwLOaQvWFAsASg0l6q7KGO3hcf2fE1IzdCz7dodCO2mvHqyDfCS8VuGyLuzpatUiQvCf7BhbLQPisqPomDYyJVaf5MvkEkUpIY%2BnYtLi4nPHmp52xnGMauNiTHZywzSdnr1WLAXsc0ncDpUqEDbPfvWzofAzha91PUAJA2YppfsMAwLGq%2FbXNIg2fdVE8IkzEWcn%2FZNSnDmOpArj877AWpIFQKCwbjNgIhmlcmTNFmyfRBLKFl%2FqoXYYCR4VTK6yXeYou3Q%2BS7AFGEqVSEfndc6p94J%2F6aW8OMhizwcc3vxhjLVFQb0nKkkrzivlxJ%2F0IHs0QasjWzrkNWLT8sOaYyFJQFPccnJMhp%2F%2FvoRGCoo3xcsrXrDlDwXQnQFIrM49OOdLttdvMElTUL4ybFUeuqTffWdP9M7%2FGVbmUrnuAIglb6FIjwdxsgihGxchnnuLK8H08XGxxlo0SDrYCU7cJigMEALFIzQ4feloUN0euONllDsoPc%2BivqXqhaiTqDWlYewsnpLvJvv5IltBqFT5Jusl1c1AAAalQGrRqOKSeYhqtBhc3En0gtGBW98oxrshOWyI5quY5ZyI9GTw2"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
//        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));
        headers.put("X-Csrf-Token",ListUtil.toList("a711Ecbt-fLQ6KljipD8H-3X5O9JgnKPpMhg"));
//        String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
//        HttpResponse execute = HttpUtil.createGet(url)
//                .header(headers)
//                .execute();
//        String body = execute.body();
//        JSONObject object = JSONUtil.parseObj(body);

//        String url = "https://iforgot.apple.com/password/verify/appleid";
//        String appleId = "wnsnjkall@outlook.com";
//        String capId   ="859718439";
//        String capToken = "a24411c37e943dc646d7de27a6b9f35b14516caf9ea5021ace947d3a311de68e97fac6a3af016fee0a61980fccace9c4d5a9541255d70fadad83621ddf4daac28ef22e687fc76b737bfcb7d6941f3b2b37e6296727cadfc9784abc0ddb76d1d17a7fe9c87a94af90a447b26d436540f4d94f25fe68d2f5a24e48f2c2b03e4aba84b9aa2b651ca40b57e75e4284081258e005b75ef20ffcd524e1fa0adada7396ae7e08da4f4eb5f53822407942b0d43fdf814935f04f48de81c5817c22fef03a17df3e094e38c6848b609632e62c1e93LIKI";
//        String answer   = "p8n1";
//        String body = "{\"id\":\""+appleId+"\",\"captcha\":{\"id\":"+capId+",\"answer\":\""+answer+"\",\"token\":\""+capToken+"\"}}\n";
//        HttpResponse execute = HttpUtil.createPost(url)
//                .body(body)
//                .header(headers)
//                .execute();
//
//        System.out.println(execute.body());

        String url = "https://privacy.apple.com/section/delete-account";
        HttpResponse execute = HttpUtil.createGet(url)
                .header(headers)
                .execute();
        System.out.println(execute.body());
    }

}

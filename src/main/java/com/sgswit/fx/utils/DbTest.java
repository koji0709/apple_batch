package com.sgswit.fx.utils;

import com.mifmif.common.regex.Generex;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import  cn.hutool.http.HttpUtil;
/**
 *
 */
public class DbTest {

    public static void main(String[] args) throws SQLException {
//        List<Entity> entityList = Db.use().query("SELECT * FROM apple_id_base_info");
//        System.err.println(entityList);


        String regExp = "X[a-zA-Z0-9]{15}";
        Generex generex = new Generex(regExp);
//        for (int i=0;i<100;i++){
//            System.out.println(generex.random().toUpperCase());
//        }


//        String cCreditInfoRegex = ".+----.+";
//        if("djabdel_94@hotmail.com----Wei100287..".matches(cCreditInfoRegex)){
//            System.out.println("1");
//        }

        String url ="https://www.baidu.com?a=zhangsan&b=2&c=2&a=张三";
        Map<String, List<String>> stringListMap = HttpUtil.decodeParams(url, "UTF-8");
        System.out.println("decodeParams：" + stringListMap);

// 获取单值map最后一个会覆盖上一个
        Map<String,String> stringStringMap = HttpUtil.decodeParamMap(url, StandardCharsets.UTF_8);
        System.out.println("decodeParamMap：" + stringStringMap);

// map转URL params,中文会自动进行转码
        String urlParams = HttpUtil.toParams(stringStringMap);
        System.out.println("urlParams: "+urlParams);

    }




}

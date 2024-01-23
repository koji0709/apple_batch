package com.sgswit.fx.utils;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.mifmif.common.regex.Generex;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class DbTest {

    public static void main(String[] args) throws SQLException {
//        List<Entity> entityList = Db.use().query("SELECT * FROM apple_id_base_info");
//        System.err.println(entityList);


        String regExp = "X[a-zA-Z0-9]{15}";
        Generex generex = new Generex(regExp);
        for (int i=0;i<100;i++){
            System.out.println(generex.random().toUpperCase());
        }


//        String cCreditInfoRegex = ".+----.+";
//        if("djabdel_94@hotmail.com----Wei100287..".matches(cCreditInfoRegex)){
//            System.out.println("1");
//        }



    }




}

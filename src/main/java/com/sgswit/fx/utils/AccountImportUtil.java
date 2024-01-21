package com.sgswit.fx.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.javafaker.Faker;
import com.sgswit.fx.model.Account;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 账号导入工具
 */
public class AccountImportUtil<T>{

    public static final String REPLACE_MENT = "####.##";

    private static final Map<String,String> kvMap = new HashMap<>(){{
        put("account","账号");
        put("pwd","密码");
        put("answer1","问题1");
        put("answer2","问题2");
        put("answer3","问题3");
        put("phone","手机号");
        put("birthday","生日");
        put("email","邮箱");
        put("paymentAccount","付款账号");
        put("paymentPwd","付款账号密码");
        put("memberAccount","成员账号");
        put("memberPwd","成员账号密码");
        put("cvv","安全码（CVV）");
        put("giftCardCode","礼品卡");
    }};

    public static String buildNote(List<String> formats){
        String result = "";
        for (int i = 0; i < formats.size(); i++) {
            String format = formats.get(i);
            result = i == 0 ? format : result + " 或 " + format;
        }
        for (String key : kvMap.keySet()) {
            if (result.contains(key)){
                result = result.replace(key,kvMap.get(key));
            }
        }
        return result;
    }

    public List<T> parseAccount(Class<T> clz,String accountStr, List<String> formatList){
        formatList = formatList.stream().map(format -> format.replaceAll("----","-")).collect(Collectors.toList());
        accountStr = accountStr.replaceAll("----","-");

        if (StrUtil.isEmpty(accountStr)){
            Console.log("导入账号为空");
            return null;
        }

        if (accountStr.contains("{-}")){
            accountStr = accountStr.replace("{-}",REPLACE_MENT);
        }

        String[] accList = accountStr.split("\n");
        if (accList.length == 0){
            return null;
        }

        List<T> accountList = new ArrayList<>();

        for (int i = 0; i < accList.length; i++) {
            String acc = accList[i];
            List<String> fieldValueList = Arrays.asList(acc.split("-"));

            Map<Integer, List<String>> formatMap = formatList
                    .stream()
                    .collect(
                        Collectors.toMap(
                                key -> key.split("-").length,
                                value -> Arrays.asList(value.split("-"))
                        )
                    );

            List<String> fieldList = formatMap.get(fieldValueList.size());
            if (fieldList == null){
                Console.log("账号导入格式不正确: Account: {}",acc);
                continue;
            }

            T account;
            try {
                account = clz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            for (int j = 0; j < fieldValueList.size(); j++) {
                String field = fieldList.get(j);
                String fieldValue = fieldValueList.get(j);
                fieldValue = fieldValue.replace(REPLACE_MENT,"-");
                ReflectUtil.invoke(
                        account
                        , "set" + field.substring(0, 1).toUpperCase() + field.substring(1)
                        , fieldValue);
            }
            accountList.add(account);
        }
        return accountList;
    }

    public static void genTestData(String format){
        Faker faker = new Faker(Locale.CHINA);
        for (int i = 1; i <= 50; i++) {
            String data = format.replace("account", faker.name().fullName())
                                .replace("pwd", RandomUtil.randomString(6))
                                .replace("answer1", faker.animal().name())
                                .replace("answer2", faker.cat().name())
                                .replace("answer3", faker.dog().name())
                                .replace("phone", faker.phoneNumber().phoneNumber())
                                .replace("birthday", DateUtil.format(faker.date().birthday(),"yyyyMMdd"))
                                .replace("email", RandomUtil.randomString(5)+"@gmail.com");
            System.err.println(data);
        }
    }

    public static void main(String[] args) {
        String format1 = "account----pwd";
        String format2 = "account----pwd-birthday";
        String format3 = "account----pwd-answer1-answer2-answer3";
        String format4 = "account----answer1-answer2-answer3-birthday";
        String format5 = "account----pwd-answer1-answer2-answer3-birthday";
        String format6 = "account----pwd-answer1-answer2-answer3-email";


        genTestData(format4);
    }



}

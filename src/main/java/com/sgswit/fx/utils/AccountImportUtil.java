package com.sgswit.fx.utils;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.model.Account;

import java.util.*;

/**
 * 账号导入工具
 */
public class AccountImportUtil {

    private static final String REPLACE_MENT = "####.##";

    private static final Map<String,String> kvMap = new HashMap<>(){{
        put("account","账号");
        put("pwd","密码");
        put("answer1","问题1");
        put("answer2","问题2");
        put("answer3","问题3");
        put("phone","手机号");
        put("birthday","生日");
        put("email","邮箱");
    }};

    public static String buildNote(String format){
        String result = format;
        for (String key : kvMap.keySet()) {
            if (format.contains(key)){
                result = result.replace(key,kvMap.get(key));
            }
        }
        return result;
    }

    public static List<Account> parseAccount(String format,String accountStr){
        List<String> fieldList = Arrays.asList(format.split("-"));

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

        List<Account> accountList = new ArrayList<>();

        for (int i = 0; i < accList.length; i++) {
            String acc = accList[i];
            List<String> fieldValueList = Arrays.asList(acc.split("-"));

            if (fieldValueList.size() != fieldList.size()){
                Console.log("账号导入格式不正确");
                continue;
            }

            Account account = new Account();
            for (int j = 0; j < fieldList.size(); j++) {
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

    public static void main(String[] args) {
        String format = "account-pwd-answer1-answer2-answer3-birthday";
        String note = buildNote(format);
        System.err.println(note);

        String accountStr = "1-1-1-2-3\n" +
                "2-2-1-2-3{-}-1996{-}08{-}10\n" +
                "3-3-1-2{-}-3\n" +
                "4-4-1{-}-2-3";

        List<Account> accountList = parseAccount(format, accountStr);
        System.err.println(accountList);

    }



}

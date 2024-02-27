package com.sgswit.fx.utils;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;

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
        put("name","姓名");
        put("nationalId","身份证号码");
        put("phone","手机号码");
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
        if (accountStr.contains("{-}")){
            accountStr = accountStr.replace("{-}",REPLACE_MENT);
        }
        accountStr= StringUtils.replacePattern(accountStr, "-| ", " ").trim();
        accountStr= CustomStringUtils.replaceMultipleSpaces(accountStr,"-");

        if (StrUtil.isEmpty(accountStr)){
            Console.log("导入账号为空");
            return new ArrayList<>();
        }

        String[] accList = accountStr.split("\n");
        if (accList.length == 0){
            return new ArrayList<>();
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

            int maxKey = Collections.max(formatMap.keySet());
            int minKey = Collections.min(formatMap.keySet());
            List<String> fieldList = formatMap.get(maxKey);
            T account;
            try {
                account = clz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if(fieldValueList.size()<minKey){
                return new ArrayList<>();
            }
            int limit=fieldValueList.size()>maxKey?maxKey:fieldValueList.size();
            for (int j = 0; j < limit; j++) {
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
    public static String[] parseAccountAndPwd(String accountStr){
        if (accountStr.contains("{-}")){
            accountStr = accountStr.replace("{-}",REPLACE_MENT);
        }
        accountStr= StringUtils.replacePattern(accountStr, "-| ", " ").trim();
        accountStr= CustomStringUtils.replaceMultipleSpaces(accountStr,"-");
        String [] arr=accountStr.split("-");
        for(int i=0;i<arr.length;i++){
            arr[i]=arr[i].replace(REPLACE_MENT,"-");
        }
        return arr;
    }
}

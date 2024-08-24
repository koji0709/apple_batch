package com.sgswit.fx.utils;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 账号导入工具
 */
public class AccountImportUtil<T>{
    /**分割字符串**/
    public static final String SPLIT_STRING = "\\{`}";
    /**替换字符串**/
    public static final String REPLACE_MEANT = "\\{*}";
    /**邮箱格式**/
    public static final String regex = "\u4e00-\u9fa5a-zA-Z0-9._%+-";

    private static final Map<String,String> kvMap = new HashMap<>(){{
        put("account","账号");
        put("pwd","密码");
        put("answer1","问题1");
        put("answer2","问题2");
        put("answer3","问题3");
        put("phone","手机号");
        put("birthday","生日(yyyyMMdd)");
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
        if (StrUtil.isEmpty(accountStr)){
            return new ArrayList<>();
        }
        String[] accList = accountStr.split("\n");
        if (accList.length == 0){
            return new ArrayList<>();
        }

        List<T> accountList = new ArrayList<>();

        for (int i = 0; i < accList.length; i++) {
            String acc = accList[i];
            if(StringUtils.isEmpty(acc)){
                continue;
            }
            List<String> fieldValueList = Arrays.asList(parseAccountAndPwd(acc));

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
        String account="";
        String pwd="";
        accountStr= StrUtils.replaceMultipleSpaces(accountStr,SPLIT_STRING);
        String[]  array=accountStr.split(SPLIT_STRING);
        if(array.length>=2){
            account=array[0];
            pwd=array[1].replace("{-}", REPLACE_MEANT);
        }else{
            boolean isEmailStarted=checkIfEmailStarted(accountStr);
            if(isEmailStarted){
                account=getEmailByStr(accountStr);
                pwd= accountStr.substring(accountStr.lastIndexOf(account)+account.length()).replace("{-}", REPLACE_MEANT);
            }else{
                accountStr=accountStr.replace("{-}", REPLACE_MEANT);
                accountStr= StringUtils.replacePattern(accountStr, "-| ", " ").trim();
                accountStr= StrUtils.replaceMultipleSpaces(accountStr,SPLIT_STRING);
                String []accountArr=accountStr.split(SPLIT_STRING,2);
                account=accountArr[0];
                if(accountArr.length>1){
                    pwd=accountArr[1];
                }
            }
        }
        pwd= StringUtils.replacePattern(pwd, "-| ", " ").trim();
        pwd= StrUtils.replaceMultipleSpaces(pwd,SPLIT_STRING).replace(REPLACE_MEANT,"-");
        List<String> list=new ArrayList<>();
        if(!StringUtils.isEmpty(account)){
            list.add(account);
        }
        if(!StringUtils.isEmpty(pwd)){
            String[] a=pwd.split(SPLIT_STRING);
            for(int i=0;i<a.length;i++){
                list.add(a[i]);
            }
        }
        return list.stream().toArray(String[]::new);
    }
    public static boolean checkIfEmailStarted(String inputStr) {
        // 定义邮箱格式的正则表达式
        Pattern pattern = Pattern.compile("^["+regex+"]+@["+regex+"]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(inputStr);
        // 返回true表示输入字符串以邮箱格式开头，false表示不是
        return matcher.find();
    }

    public static String getEmailByStr(String text) {
        // 定义电子邮件地址的正则表达式模式
        Pattern pattern = Pattern.compile("["+regex+"]+@["+regex+"]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(text);
        String firstEmail=null;
        while (matcher.find() && StringUtils.isEmpty(firstEmail)) {
            firstEmail =matcher.group();
        }
        return firstEmail;
    }
}

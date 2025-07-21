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
    public static final String SPLIT_STRING = "；";
    /**替换字符串**/
    public static final String REPLACE_MEANT = "？";
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
        accountStr = accountStr.trim();
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
            acc = acc.trim();
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
            list.add(account.replace(REPLACE_MEANT,"-"));
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

    public static class AccountParser {

        private static final Pattern PATTERN = Pattern.compile(
                "^\\s*(?<username>[\\w？@.\\-]+)" +
                        "(?:[\\s]+(?<password>[^\\s]*))?" +
                        "[\\s]+(?<q1>.+?)" +
                        "[\\s]+(?<q2>.+?)" +
                        "[\\s]+(?<q3>.+?)" +
                        "[\\s]+(?<birthDate>\\d{4}(?:[-/]?\\d{1,2}){2})\\s*$"
        );
        private static Map<String, String> parse(String line) {
            Matcher matcher = PATTERN.matcher(line);
            if (!matcher.matches()) {
                return Collections.emptyMap();
            }
            Map<String, String> result = new LinkedHashMap<>();
            result.put("username", matcher.group("username").replaceAll(AccountImportUtil.REPLACE_MEANT,"-"));
            result.put("password", Optional.ofNullable(matcher.group("password")).orElse("").replaceAll(AccountImportUtil.REPLACE_MEANT,"-"));
            result.put("securityQuestion1", matcher.group("q1"));
            result.put("securityQuestion2", matcher.group("q2"));
            result.put("securityQuestion3", matcher.group("q3"));
            result.put("birthDate", normalizeDate(matcher.group("birthDate")));

            return result;
        }
        private static Map<String, String> parseBySemicolon(String input) {
            Map<String, String> result = new LinkedHashMap<>();
            // 提取出生日期
            Pattern datePattern = Pattern.compile(
                    "\\b(?:\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}|\\d{8})\\b"
            );

            Matcher matcher = datePattern.matcher(input);
            String birthDate = null;
            if (matcher.find()) {
                birthDate = normalizeDate(matcher.group());
                input = input.substring(0, matcher.start()).trim(); // 去掉出生日期部分
            }
            if (birthDate != null) {
                result.put("birthDate", birthDate);
            }
            // 拆分剩下部分
            String[] arr = input.split(AccountImportUtil.SPLIT_STRING);
            int len = arr.length;
            if (len == 0) {
                return result;
            }

            result.put("username",replaceXX(arr[0]));

            if (len == 1) {
                // 只有用户名
                return result;
            }else if (len == 2) {
                // 用户名 + 密码
                result.put("password", replaceXX(arr[1]));
                return result;
            }else if (len >= 3) {
                result.put("password", "");
                // 判断 arr[1] 是否为密码：如果总长度 >= 5，则 arr[1] 是密码，否则就是密保
                if (len >= 5) {
                    result.put("password", replaceXX(arr[1]));
                    result.put("securityQuestion1", arr[2]);
                    result.put("securityQuestion2", arr[3]);
                    result.put("securityQuestion3", arr[4]);
                } else if (len == 4) {
                    result.put("securityQuestion1", arr[1]);
                    result.put("securityQuestion2", arr[2]);
                    result.put("securityQuestion3", arr[3]);
                } else if (len == 3) {
                    result.put("securityQuestion1", arr[1]);
                    result.put("securityQuestion2", arr[2]);
                }
            }
            return result;
        }
        private static String normalizeDate(String rawDate) {
            // 将日期格式统一为 yyyy-MM-dd
            rawDate = rawDate.replaceAll("[/]", "-");
            String[] parts = rawDate.split("-");
            if (parts.length == 3) {
                String y = parts[0];
                String m = String.format("%02d", Integer.parseInt(parts[1]));
                String d = String.format("%02d", Integer.parseInt(parts[2]));
                return y + "-" + m + "-" + d;
            } else if (rawDate.matches("\\d{8}")) {
                return rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6, 8);
            }
            return rawDate;
        }
        public static Map<String, String> parseAccountToMap(String input) {
            input=input.replaceAll("\\{-\\}", AccountImportUtil.REPLACE_MEANT);
            Pattern pattern = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
            Matcher matcher = pattern.matcher(input);
            String output = matcher.replaceAll("$1/$2/$3");
            output=output.replaceAll("[\\t|\\r|-]+",  AccountImportUtil.SPLIT_STRING);
            Map<String, String> parsed = parse(output);
            if(parsed.isEmpty()){
                return parseBySemicolon(output);
            }else{
                return parsed;
            }
        }
        private static String replaceXX(String str){
            return str.replace(AccountImportUtil.REPLACE_MEANT,"-");
        }
    }
}

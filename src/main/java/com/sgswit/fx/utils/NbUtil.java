package com.sgswit.fx.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 乱取的名字, 获取语言和问题什么的
 */
public class NbUtil {

    public static List<String> getLanguageList(){
        return  getLanguageMap().keySet().stream().map(e -> e.toString()).collect(Collectors.toList());
    }

    public static LinkedHashMap<String,String> getLanguageMap(){
        String lang = ResourceUtil.readUtf8Str("json/language.json");
        return JSONUtil.parse(lang).toBean(LinkedHashMap.class);
    }

    public static List<List<String>> getQuestionList(){
        String questions = ResourceUtil.readUtf8Str("json/questions.json");
        JSONArray jsonArray = JSONUtil.parseArray(questions);
        List<List<String>> resultList = new ArrayList<>();
        for (Object o : jsonArray) {
            JSONArray array = (JSONArray) o;
            List<String> list = new ArrayList<>();
            for (Object object : array) {
                JSONObject json = (JSONObject) object;
                list.add(json.getStr("question"));
            }
            resultList.add(list);
        }
        return resultList;
    }

    public static LinkedHashMap<String,Integer> getQuestionMap(){
        String questions = ResourceUtil.readUtf8Str("json/questions.json");
        JSONArray jsonArray = JSONUtil.parseArray(questions);
        LinkedHashMap<String,Integer> resultMap =  new LinkedHashMap<>();
        for (Object o : jsonArray) {
            JSONArray array = (JSONArray) o;
            for (Object object : array) {
                JSONObject json = (JSONObject) object;
                resultMap.put(json.getStr("question"),json.getInt("id"));
            }
        }
        return resultMap;
    }

    // todo 临时 之后可能会用数据库存储账号数据
    public static List<Account> getAccountList(){
        String accountStr = ResourceUtil.readUtf8Str("json/account.json");
        JSONArray jsonArray = JSONUtil.parseArray(accountStr);
        if (CollectionUtil.isEmpty(jsonArray)){
            return Collections.emptyList();
        }

        List<Account> accountList1 = new ArrayList<>();
        for (Object o : jsonArray) {
            JSONObject json = (JSONObject) o;
            Account account = new Account();
            account.setAccount(json.getStr("account"));
            account.setPwd(json.getStr("pwd"));
            account.setState(json.getStr("state"));
            account.setAera(json.getStr("aera"));
            account.setName(json.getStr("name"));
            account.setStatus(json.getStr("status"));
            account.setNote(json.getStr("note"));
            account.setLogtime(json.getStr("logtime"));
            account.setAnswer1(json.getStr("answer1"));
            account.setAnswer2(json.getStr("answer2"));
            account.setAnswer3(json.getStr("answer3"));
            account.setBirthday(json.getStr("birthday"));
            accountList1.add(account);
        }

        return accountList1;
    }

    public static void main(String[] args) {
        List<Account> accountList = getAccountList();
        accountList.forEach(System.out::println);
    }

}

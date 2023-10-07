package com.sgswit.fx.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
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
        String lang = ResourceUtil.readUtf8Str("data/language.json");
        return JSONUtil.parse(lang).toBean(LinkedHashMap.class);
    }

    public static List<List<String>> getQuestionList(){
        String questions = ResourceUtil.readUtf8Str("data/questions.json");
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
        String questions = ResourceUtil.readUtf8Str("data/questions.json");
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
}

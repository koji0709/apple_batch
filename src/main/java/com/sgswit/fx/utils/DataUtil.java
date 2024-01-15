package com.sgswit.fx.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.iTunes.bo.FieldModel;
import com.sgswit.fx.model.BaseAreaInfo;
import com.sgswit.fx.utils.machineInfo.MachineInfoBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author DeZh
 * @title: DataUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1915:13
 */
public class DataUtil {
    private static List<BaseAreaInfo> baseAreaInfoList;
    private static List<Map> proxyModeList;
    /**
    　* @description: 获取国家地区码表
      * @param
    　* @return java.util.List<com.sgswit.fx.model.BaseAreaInfo>
    　* @throws
    　* @author DeZh
    　* @date 2023/9/20 8:49
    */
    public static List<Map> getProxyModeList(){
        try {
            if(null==proxyModeList || proxyModeList.size()==0){
                String jsonString = ResourceUtil.readUtf8Str("data/proxy_mode.json");
                proxyModeList= JSONUtil.toList(jsonString, Map.class);
            }
        }catch (Exception e){
            proxyModeList=new ArrayList<>();
        }
        return proxyModeList;
    }
    public static List<BaseAreaInfo> getCountry(){
        try {
            if(null==baseAreaInfoList || baseAreaInfoList.size()==0){
                String jsonString = ResourceUtil.readUtf8Str("data/support_all_country.json");
                baseAreaInfoList= JSONUtil.toList(jsonString, BaseAreaInfo.class);
            }
        }catch (Exception e){
            baseAreaInfoList=new ArrayList<>();
        }
        return baseAreaInfoList;
    }
    public static List<BaseAreaInfo> getFastCountry(){
        String jsonString = ResourceUtil.readUtf8Str("data/support_fast_country.json");
        return JSONUtil.toList(jsonString, BaseAreaInfo.class);
    }

    public static BaseAreaInfo getInfoByCountryCode(String countryCode){
        getCountry();
        List<BaseAreaInfo> list=new ArrayList<>();
        if(countryCode.length()==3){
            list= baseAreaInfoList.stream().filter(n->n.getCode().equals(countryCode)).collect(Collectors.toList());
        }else if(countryCode.length()==2){
            list= baseAreaInfoList.stream().filter(n->n.getCode2().equals(countryCode)).collect(Collectors.toList());
        }
        return (list.size()==0)?null:list.get(0);
    }
    public static String getNameByCountryCode(String countryCode){
        BaseAreaInfo areaInfo=getInfoByCountryCode(countryCode);
        return (null==areaInfo)?"":areaInfo.getNameZh();
    }
    public static String getCodeByCountryName(String countryName){
        getCountry();
        List<BaseAreaInfo> list= baseAreaInfoList.stream().filter(n->n.getNameZh().equals(countryName)).collect(Collectors.toList());
        return (list.size()==0)?null:list.get(0).getCode();
    }

    public static String getAddressFormat(String countryCode){
        Map<String,Object> result= new HashMap<>();
        try {
            List<String> fieldsList=new ArrayList<>();
            List<FieldModel> addressFormatList=new ArrayList<>();
            String jsonString = ResourceUtil.readUtf8Str("data/address_format.json");
            for(Object object:JSONUtil.parseArray(jsonString)){
                JSON json= (JSON) object;
                if(countryCode.equalsIgnoreCase(json.getByPath("code").toString())){

                    JSONObject addressFormat=JSONUtil.parseObj((json.getByPath("json")));
                    JSONObject fieldsJSONObject=addressFormat.getJSONObject("fields");
                    JSONObject sectionsJSONObject=addressFormat.getJSONObject("sections");
                    //主要字段
                    String primaryAddress=sectionsJSONObject.getByPath("primaryAddress.lines").toString();
                    List<String[]> primaryAddressList=JSONUtil.toList(primaryAddress,String[].class);
                    for( String[] arr:primaryAddressList){
                        for(String s:arr){
                            fieldsList.add(s);
                        }
                    }
                    //手机
                    String phone=sectionsJSONObject.getByPath("phone.lines").toString();
                    List<String[]> phoneList=JSONUtil.toList(phone,String[].class);
                    for( String[] arr:phoneList){
                        for(String s:arr){
                            fieldsList.add(s);
                        }
                    }
                    for(String key:fieldsList){
                        FieldModel fieldModel=JSONUtil.toBean(fieldsJSONObject.getStr(key), FieldModel.class);
                        addressFormatList.add(fieldModel);
                    }
                    result.put("addressFormatList",addressFormatList);
                    result.put("fieldsList",fieldsList);
                    break;
                }
            }
        }catch (Exception e){

        }
        return JSONUtil.toJsonStr(result);
    }
    public static void  getNews(){
        try {
            HttpResponse rsp = HttpUtil.get("/noticeInfo/getNoticeInfo");
            boolean verify = HttpUtil.verifyRsp(rsp);
            if (!verify){
            }else {
                File fFile = new File("news.ini");
                if(!fFile.exists()){
                    fFile.createNewFile();
                }
                String test=JSONUtil.parse(rsp.body()).getByPath("data.content",String.class);
                FileUtil.writeBytes(test.getBytes( Charset.defaultCharset()), fFile);
            }
        }catch (Exception e){

        }
    }
    public static String getGuidByAppleId(String appleId){
        String guid=null;
        try {
            guid= Db.use().queryString("SELECT guid FROM apple_id_base_info WHERE apple_id=?",appleId);
            if(StringUtils.isEmpty(guid)){
                guid = MachineInfoBuilder.generateMachineInfo().getMachineGuid();
                Entity entity=new Entity();
                entity.setTableName("apple_id_base_info");
                entity.set("guid",guid);
                entity.set("apple_id",appleId);
                Db.use().insertOrUpdate(entity,"apple_id");
            }
        }catch (Exception e){
            guid = MachineInfoBuilder.generateMachineInfo().getMachineGuid();
        }finally {

        }
        return guid;
    }
    public static String getClientIdByAppleId(String appleId){
        String clientId;
        try {
            clientId= Db.use().queryString("SELECT client_id FROM apple_id_base_info WHERE apple_id=?",appleId);
            if(StringUtils.isEmpty(clientId)){
                clientId = IdUtil.fastUUID().toUpperCase();
                Entity entity=new Entity();
                entity.setTableName("apple_id_base_info");
                entity.set("apple_id",appleId);
                entity.set("client_id",clientId);
                Db.use().insertOrUpdate(entity,"apple_id");
            }
        }catch (Exception e){
            clientId = IdUtil.fastUUID().toUpperCase();
        }finally {
        }
        return clientId;
    }
    public static String getWebClientIdByAppleId(String appleId){
        String clientId;
        try {
            clientId= Db.use().queryString("SELECT web_client_id FROM apple_id_base_info WHERE apple_id=?",appleId);
            if(StringUtils.isEmpty(clientId)){
                clientId = WebLoginUtil.createClientId();
                Entity entity=new Entity();
                entity.setTableName("apple_id_base_info");
                entity.set("apple_id",appleId);
                entity.set("web_client_id",clientId);
                Db.use().insertOrUpdate(entity,"apple_id");
            }
        }catch (Exception e){
            clientId = WebLoginUtil.createClientId();
        }finally {
        }
        return clientId;
    }
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

    public static List<String> getGlobalMobilePhoneRegular(){
        String mobilePhoneJson = ResourceUtil.readUtf8Str("data/global-mobile-phone-regular.json");
        JSONObject jsonObj = JSONUtil.parseObj(mobilePhoneJson);
        JSONArray mobilephoneArray = jsonObj.getJSONArray("data");
        List<String> resultList = new ArrayList<>();
        for (Object o : mobilephoneArray) {
            JSONObject json = (JSONObject) o;
            String format = "+%s（%s）";
            resultList.add(String.format(format,json.getStr("code"),json.getStr("zh")));
        }
        return resultList;
    }
    public static void main(String[] args) {
//        getGuidByAppleId("djli0506@163.com");
//        getClientIdByAppleId("djli0506@163.com");


    }
}

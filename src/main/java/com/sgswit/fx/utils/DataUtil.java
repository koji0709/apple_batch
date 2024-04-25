package com.sgswit.fx.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.iTunes.bo.FieldModel;
import com.sgswit.fx.enums.ProxyEnum;
import com.sgswit.fx.model.BaseAreaInfo;
import com.sgswit.fx.utils.machineInfo.MachineInfoBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private static Map<String,Map<String,String>> ids=new HashMap<>();
    private static String guid="guid";
    private static String client_id="cld";
    private static String web_client_id="wbCld";
    private static Map<String,Object> userInfo=new HashMap<>();
    //内置代理信息
    private static List<Map<String,Object>> proxyConfigList;
    public static List<Map<String,Object>> getProxyConfig(){
        try {
            if(null==proxyConfigList || proxyConfigList.size()==0){
                //调接口
                HttpResponse rsp = HttpUtils.get("/api/data/getProxyConfig");
                JSON json=JSONUtil.parse(rsp.body());
                if (json.getByPath("code",String.class).equals(Constant.SUCCESS)){
                    proxyConfigList=json.getByPath("data",List.class);
                }
            }
        }catch (Exception e){
            proxyConfigList=new ArrayList<>();
        }
        return proxyConfigList;
    }
    public static List<Map<String,Object>> getProxyModeList(){
        List<Map<String, Object>> proxyModeList = ProxyEnum.Mode.getProxyModeList();
        if(null==proxyConfigList || proxyConfigList.size()==0){
            // 移除属性值为"Male"的对象
            Iterator<Map<String, Object>> iterator = proxyModeList.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> map = iterator.next();
                if (ProxyEnum.Mode.DEFAULT.getKey().equals(map.get("key"))) {
                    iterator.remove();
                }
            }
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
    public static void getNews(){
        try {
            String platform=PropertiesUtil.getConfig("softwareInfo.platform");
            HttpResponse rsp = HttpUtils.get("/noticeInfo/getNoticeInfo/"+platform);
            boolean verify = HttpUtils.verifyRsp(rsp);
            if (!verify){
            }else {
                String fileName="news.ini";
                File fFile = new File(fileName);
                if(fFile.exists()){
                    fFile.delete();
                }
                JSON json=JSONUtil.parse(rsp.body());
                String title=json.getByPath("data.title",String.class);
                String content=json.getByPath("data.content",String.class);
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
                try{
                    writer.write(title+":");
                    writer.write("\n");
                    writer.write("\n");
                    writer.write(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    writer.flush();
                    writer.close();
                }
            }
        }catch (Exception e){

        }
    }
    public static String getGuidByAppleId(String appleId){
        return getId(guid,appleId);
    }
    public static String getClientIdByAppleId(String appleId){
        return getId(client_id,appleId);
    }
    public static String getWebClientIdByAppleId(String appleId){
        return getId(web_client_id,appleId);
    }
    public static List<String> getLanguageList(){
        return  getLanguageMap().keySet().stream().collect(Collectors.toList());
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
    public static int getQuestionIndex(int id){
        int index=1;
        String questions = ResourceUtil.readUtf8Str("data/questions.json");
        JSONArray jsonArray = JSONUtil.parseArray(questions);
        LinkedHashMap<String,Integer> resultMap =  new LinkedHashMap<>();
        for (int i=0;i<jsonArray.size();i++ ) {
            JSONArray array = (JSONArray) jsonArray.get(i);
            for (Object object : array) {
                JSONObject json = (JSONObject) object;
                if(id==json.getInt("id")){
                    return i+1;
                }
            }
        }
        return index;
    }


    private static String getId(String type,String appleId){
        String id=null;
        try {
            boolean f=false;
            if(null!=ids.get(type)){
              id= ids.get(type).get(appleId);
              if(StringUtils.isEmpty(id)){
                f=true;
              }
            }else{
                f=true;
                ids.put(type,new HashMap<>());
            }
            if(f){
                HttpResponse rsp = HttpUtils.get("/api/data/getId/"+type+"?appleId="+appleId);
                JSON json=JSONUtil.parse(rsp.body());
                if (json.getByPath("code",String.class).equals(Constant.SUCCESS)){
                    id= json.getByPath("id",String.class);
                }else{
                    id=generateId(type);
                }
            }
        }catch (Exception e){
            id=generateId(type);
        }
        ids.get(type).put(appleId,id);
        return id;
   }
   private static String generateId(String type){
        String id="";
        if(guid.equals(type)){
            id=MachineInfoBuilder.generateMachineInfo().getMachineGuid();
        }else if(web_client_id.equals(type)){
            id=WebLoginUtil.createClientId();
        }else if(client_id.equals(type)){
            id=IdUtil.fastUUID().toUpperCase();
        }
        return id;
   }
   public static Map<String,String> getAddressInfo(String countryCode){
       HttpResponse rsp = HttpUtils.get("/api/data/getRandAddress?countryCode="+countryCode);
       JSON json=JSONUtil.parse(rsp.body());
       if (json.getByPath("code",String.class).equals(Constant.SUCCESS)){
          return json.getByPath("data",Map.class);
       }
       return null;
   }
   /**
   　* 设置用户信息
   　* @return void
   　* @date 2024/1/30 10:46
   */
   public static void setUserInfo(String key,String val){
        userInfo.put(key,val);
   }
   public static void setUserInfo(String json){
       Map<String, Object> map = JSONUtil.parseObj(json).toBean(HashMap.class);
       userInfo=map;
   }
   public static Map<String,Object> getUserInfo(){
       return userInfo;
   }
}

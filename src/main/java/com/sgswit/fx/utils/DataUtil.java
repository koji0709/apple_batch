package com.sgswit.fx.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.iTunes.bo.FieldModel;
import com.sgswit.fx.model.BaseAreaInfo;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    /**
    　* @description: 获取国家地区码表
      * @param
    　* @return java.util.List<com.sgswit.fx.model.BaseAreaInfo>
    　* @throws
    　* @author DeZh
    　* @date 2023/9/20 8:49
    */
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
        List<BaseAreaInfo> list= baseAreaInfoList.stream().filter(n->n.getCode().equals(countryCode)).collect(Collectors.toList());
        return (list.size()==0)?null:list.get(0);
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
}

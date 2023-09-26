package com.sgswit.fx.utils;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.BaseAreaInfo;

import java.util.ArrayList;
import java.util.List;
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
                HttpResponse res = HttpUtil.createGet("http://localhost:8094/api/data/getCountry").execute();
                if(res.getStatus()==200){
                    String jsonString= JSONUtil.parseObj(res.body()).getStr("list");
                    baseAreaInfoList= JSONUtil.toList(jsonString, BaseAreaInfo.class);
                }else{
                    baseAreaInfoList=new ArrayList<>();
                }
            }
        }catch (Exception e){
            baseAreaInfoList=new ArrayList<>();
        }
        return baseAreaInfoList;
    }

    public static BaseAreaInfo getInfoByCountryCode(String countryCode){
        List<BaseAreaInfo> list= baseAreaInfoList.stream().filter(n->n.getCode().equals(countryCode)).collect(Collectors.toList());
        return (list.size()==0)?null:list.get(0);
    }



    public static String getAddressFormat(String countryCode){
        try {
            String url="http://localhost:8094/api/data/getAddressFormat/"+countryCode;
            HttpResponse res = HttpUtil.createGet(url).execute();
            if(res.getStatus()==200){
                return res.body();
            }
        }catch (Exception e){
        }
        return null;
    }
}

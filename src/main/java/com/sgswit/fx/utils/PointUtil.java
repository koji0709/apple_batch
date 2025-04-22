package com.sgswit.fx.utils;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.enums.StageEnum;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DeZh
 * @title: PointUtil
 * @projectName appleBatch
 * @description: 点数工具类型
 * @date 2024/1/1211:34
 */
public class PointUtil {
    public static String out="-";
    public static String in="+";
    public static List<Map> pointConfigList=new ArrayList<>();;
    /**
    　* 点数消耗记录
      * @param
     * @param functionCode 功能代码，对应枚举类FunctionListEnum
     * @param type 类型，“-”代表消耗，“+”代表返还
     * @param appleId
     * @param notes 备注
    　* @return java.util.Map<java.lang.String,java.lang.String>
    　* @throws
    　* @author DeZh
    　* @date 2024/1/12 14:21
    */
    public static Map<String,String> pointCost(String functionCode,String type,String appleId,String notes,int num,String flag){
        Map<String,String> res=  new HashMap<>();
        try{
            //获取当前登录账号的用户名
            //String username=PropertiesUtil.getOtherConfig("login.userName");
            String username = LoginUtil.getUserName();
            username=SignUtil.decryptBase64(username);
            Map<String,Object> map=new HashMap<>();
            map.put("functionCode",functionCode);
            map.put("type",type);
            map.put("appleId",appleId);
            map.put("notes",notes);
            map.put("username",username);
            map.put("num",num);
            map.put("flag",flag);
            String platform=PropertiesUtil.getConfig("softwareInfo.platform");
            map.put("platform", "1".equals(platform)?"win":"mac");
            String body = JSONUtil.toJsonStr(map);
            HttpResponse rsp = HttpUtils.post("/api/data/pointCost",body);
            JSON json=JSONUtil.parse(rsp.body());
            if (json.getByPath("code",String.class).equals(Constant.SUCCESS)){
                res.put("code", Constant.SUCCESS);
                refreshRemainingPointsUI(json.getByPath("data.remainingPoints",String.class));
            }else{
                res.put("code", "-1");
                res.put("msg", json.getByPath("msg",String.class));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }
    public static Map<String,String> pointCost(String functionCode,String type,String appleId){
        return pointCost(functionCode,type,appleId,"");
    }
    public static Map<String,String> pointCost(String functionCode,String type,String appleId,String reason){
        String notes="";
        if(out.equals(type)){
            notes="成功后自动扣除";
        }else{
            notes="失败后返还";
        }
        if(!StringUtils.isEmpty(reason) && in.equals(type)){
            notes=MessageFormat.format(notes+"。失败原因：{0}", new String[]{reason});
        }
        return pointCost(functionCode,type,appleId,notes,1,"");
    }
    public static Map<String,String> pointCost(String functionCode, List list){
        int num=0;
        for(Object account:list){
            Object note= ReflectUtil.getFieldValue(account, "getNote");
            if(null==note||"".equals(note)){
                num++;
            }
        }
        return pointCost(functionCode,out,"","",num,"all");
    }
    public static void refreshRemainingPoints(String remainingPoints){
        Stage main= StageUtil.get(StageEnum.MAIN);
        Parent root =main.getScene().getRoot();
        Label label = (Label) root.lookup("#remainingPoints");
        label.setText(remainingPoints);
    }
    public static void refreshRemainingPointsUI(String points){
        Platform.runLater(new Task<Integer>() {
            @Override
            protected Integer call() {
                refreshRemainingPoints(points);
                return 1;
            }
        });
    }
    public static void getPointConfig() {
        try {
            if(null==pointConfigList || pointConfigList.size()==0){
                HttpResponse rsp = HttpUtils.get("/api/data/getPointConfig");
                JSON json=JSONUtil.parse(rsp.body());
                if (json.getByPath("code",String.class).equals(Constant.SUCCESS)){
                    pointConfigList= JSONUtil.toList(json.getByPath("data",String.class), Map.class);
                }
            }
        }catch (Exception e){

        }
    }
    /**
    　* 根据代码获取点数
      * @param
     * @param code
    　* @return int
    　* @throws
    　* @author DeZh
    　* @date 2024/1/15 20:08
    */
    public static int getPointByCode(String code) {
        int point=0;
        try {
            if(null==pointConfigList || pointConfigList.size()==0){
                point=FunctionListEnum.getFunEnumByCode(code).getPoint();
            }else {
                Object pointObj=pointConfigList.stream().filter(n->n.get("functionCode").equals(code)).findAny().get().get("point");
                point=Integer.valueOf(pointObj.toString());
            }
        }catch (Exception e){

        }
        return point;
    }
}

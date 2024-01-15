package com.sgswit.fx.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
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

import java.util.HashMap;
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
    public static Map<String,String> pointCost(String functionCode,String type,String appleId,String notes){
        Map<String,String> res=  new HashMap<>();
        try{
            //获取当前登录账号的用户名
            String username=PropertiesUtil.getOtherConfig("login.userName");
            FunctionListEnum anEnum=FunctionListEnum.getFunEnumByCode(functionCode);
            Map<String,String> map=new HashMap<>();
            map.put("functionCode",functionCode);
            map.put("type",type);
            map.put("appleId",appleId);
            map.put("notes",notes);
            map.put("username",username);
            String body = JSONUtil.toJsonStr(map);
            HttpResponse rsp = HttpUtil.post("/api/data/pointCost",body);
            JSON json=JSONUtil.parse(rsp.body());
            if (json.getByPath("code",String.class).equals(Constant.SUCCESS)){
                res.put("code", Constant.SUCCESS);
                refreshRemainingPointsUI(json.getByPath("data.remainingPoints",String.class));
            }else{
                res.put("code", "-1");
                res.put("msg", json.getByPath("msg",String.class));
            }
        }catch (Exception e){

        }
        return res;
    }
    public static Map<String,String> pointCost(String functionCode,String type,String appleId){
        String notes="";
        if(out.equals(type)){
            notes="成功后自动扣除";
        }else{
            notes="失败后返还";
        }
        return pointCost(functionCode,type,appleId,notes);
    }
    public static void refreshRemainingPoints(String remainingPoints){
        Stage main= StageUtil.get(StageEnum.MAIN);
        Parent root =main.getScene().getRoot();
        Label label = (Label) root.lookup("#remainingPoints");
        label.setText(remainingPoints);
    }
    public static void refreshRemainingPointsUI(String remainingPoints){
        Platform.runLater(new Task<Integer>() {
            @Override
            protected Integer call() {
                refreshRemainingPoints(remainingPoints);
                return 1;
            }
        });
    }
}

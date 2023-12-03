package com.sgswit.fx.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.SecuritycodePopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author DeZh
 * @title: 通用部分
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/2615:27
 */
public class CommController<T> {
    /**
    　* 操作方法
      * @param
     * @param account
     * @param step1Res
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/11/23 16:31
    */
    protected void queryOrUpdate(T account, HttpResponse step1Res){
    }
    /**
    　* 添加本地记录
      * @param
     * @param account
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/11/23 16:31
    */
    protected void insertLocalLog(T account){

    }

    /**
    　* 点击执行查询
      * @param
     * @param accoutQueryBtn
     * @param accountTableView
     * @param list
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/10/26 23:00
    */
    protected void onAccountQueryBtnClick(Button accoutQueryBtn,TableView accountTableView,ObservableList<T> list) throws Exception{
        int n=0;
        //判断是否有待执行的数据
        for(T account:list) {
            //判断是否已执行或执行中,避免重复执行
            Object note = getFieldValueByObject(account, "note");
            if (!StrUtil.isEmptyIfStr(note)) {
                continue;
            }else{
                n++;
            }
        }
        if(n==0){
            return;
        }
        for(T account:list){
            //判断是否已执行或执行中,避免重复执行
            Object note=getFieldValueByObject(account,"note");
            if(!StrUtil.isEmptyIfStr(note)){
                continue;
            }
            Object answer1=getFieldValueByObject(account,"answer1");
            if(StrUtil.isEmptyIfStr(answer1)){
                //双重认证
                secondSec(account,accoutQueryBtn,accountTableView);
            }else{
                //非双重认证
                accoutQueryBtn.setText("正在查询");
                accoutQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
                accoutQueryBtn.setDisable(true);

                setFieldValueByObject(account, "正在登录...","note");
                accountTableView.refresh();

                new Thread(new Runnable() {
                    @Override
                    public void run(){
                        try {
                            try {
                                noSecondSec(account,accoutQueryBtn,accountTableView);
                            } catch (Exception e) {
                                accoutQueryBtn.setDisable(false);
                                accoutQueryBtn.setText("开始执行");
                                accoutQueryBtn.setTextFill(Paint.valueOf("#238142"));
                                e.printStackTrace();
                            }
                        }finally {
                            //JavaFX Application Thread会逐个阻塞的执行这些任务
                            Platform.runLater(new Task<Integer>() {
                                @Override
                                protected Integer call() {
                                    accoutQueryBtn.setDisable(false);
                                    accoutQueryBtn.setText("开始执行");
                                    accoutQueryBtn.setTextFill(Paint.valueOf("#238142"));
                                    return 1;
                                }
                            });
                        }
                    }
                }).start();
            }

        }
    }

    private boolean secondSec(T account,Button accoutQueryBtn,TableView accountTableView) throws Exception {
        //step1 sign 登录
        Object accountName=getFieldValueByObject(account,"account");
        Object pwd=getFieldValueByObject(account,"pwd");
        Object answer1=getFieldValueByObject(account,"answer1");
        Object answer2=getFieldValueByObject(account,"answer2");
        Object answer3=getFieldValueByObject(account,"answer3");

        Account a=new Account();
        a.setPwd(pwd.toString());
        a.setAccount(accountName.toString());
        a.setAnswer1(StrUtil.isEmptyIfStr(answer1)?"":answer1.toString());
        a.setAnswer2(StrUtil.isEmptyIfStr(answer2)?"":answer2.toString());
        a.setAnswer3(StrUtil.isEmptyIfStr(answer3)?"":answer3.toString());
        HttpResponse step1Res = AppleIDUtil.signin(a);

        if (step1Res.getStatus() != 409) {
            queryFail(account, accoutQueryBtn, accountTableView);
            insertLocalLog(account);
            return false;
        }
        String step1Body = step1Res.body();
        JSON json = JSONUtil.parse(step1Body);
        if (json == null) {
            queryFail(account, accoutQueryBtn, accountTableView);
            return false;
        }

        //step2 auth 获取认证信息
        HttpResponse step21Res = AppleIDUtil.auth(step1Res);
        String authType = (String) json.getByPath("authType");
        if ("hsa2".equals(authType)) {
            // 双重验证
            //step2.2 输入验证码
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/securitycode-popup.fxml"));

                Scene scene = new Scene(fxmlLoader.load(), 600, 350);
                scene.getRoot().setStyle("-fx-font-family: 'serif'");

                SecuritycodePopupController s = (SecuritycodePopupController) fxmlLoader.getController();
                s.setAccount(a.getAccount());

                Stage popupStage = new Stage();
                popupStage.setTitle("双重验证码输入页面");
                popupStage.initModality(Modality.WINDOW_MODAL);
                popupStage.setScene(scene);
                popupStage.showAndWait();

                String type = s.getSecurityType();
                String code = s.getSecurityCode();

                accoutQueryBtn.setText("正在查询");
                accoutQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
                accoutQueryBtn.setDisable(true);

                setFieldValueByObject(account, "正在验证验证码...","note");
                accountTableView.refresh();

                new Thread(new Runnable() {
                    @Override
                    public void run(){
                        try {
                            HttpResponse step22Res = AppleIDUtil.securityCode(step21Res, type, code);
                            if (step22Res.getStatus() != 204 && step22Res.getStatus() != 200) {
                                queryFail(account, accoutQueryBtn, accountTableView);
                            }
                            setFieldValueByObject(account, "登录成功","note");
                            accountTableView.refresh();
                            queryOrUpdate(account, step22Res);
                        } catch (Exception e) {
                            accoutQueryBtn.setDisable(false);
                            accoutQueryBtn.setText("开始执行");
                            accoutQueryBtn.setTextFill(Paint.valueOf("#238142"));
                            e.printStackTrace();
                        } finally {
                            //JavaFX Application Thread会逐个阻塞的执行这些任务
                            Platform.runLater(new Task<Integer>() {
                                @Override
                                protected Integer call() {
                                    accoutQueryBtn.setDisable(false);
                                    accoutQueryBtn.setText("开始执行");
                                    accoutQueryBtn.setTextFill(Paint.valueOf("#238142"));
                                    return 1;
                                }
                            });
                        }
                    }
                }).start();


            }catch (Exception e){
                return false;
            }

        }else if ("sa".equals(authType)) {
            setFieldValueByObject(account, "该账户为非双重认证模式，请输入密保信息后重试","note");
            accountTableView.refresh();
            insertLocalLog(account);
        }
        return true;
    }

    private boolean noSecondSec(T account,Button accoutQueryBtn,TableView accountTableView) throws Exception {
        //step1 sign 登录
        Object accountName=getFieldValueByObject(account,"account");
        Object pwd=getFieldValueByObject(account,"pwd");
        Object answer1=getFieldValueByObject(account,"answer1");
        Object answer2=getFieldValueByObject(account,"answer2");
        Object answer3=getFieldValueByObject(account,"answer3");

        Account a=new Account();
        a.setPwd(pwd.toString());
        a.setAccount(accountName.toString());
        a.setAnswer1(StrUtil.isEmptyIfStr(answer1)?"":answer1.toString());
        a.setAnswer2(StrUtil.isEmptyIfStr(answer2)?"":answer2.toString());
        a.setAnswer3(StrUtil.isEmptyIfStr(answer3)?"":answer3.toString());
        HttpResponse step1Res = AppleIDUtil.signin(a);

        if (step1Res.getStatus() != 409) {
            queryFail(account, accoutQueryBtn, accountTableView);
            insertLocalLog(account);
            return false;
        }
        String step1Body = step1Res.body();
        JSON json = JSONUtil.parse(step1Body);
        if (json == null) {
            queryFail(account, accoutQueryBtn, accountTableView);
            insertLocalLog(account);
            return false;
        }

        String authType = (String) json.getByPath("authType");
        if ("sa".equals(authType)) {
            //非双重认证
            //step2 获取认证信息 -- 需要输入密保
            HttpResponse step21Res = AppleIDUtil.auth(step1Res);
            setFieldValueByObject(account, "正在验证密保问题...","note");
            accountTableView.refresh();
            HttpResponse step211Res = AppleIDUtil.questions(step21Res, a);
            if (step211Res.getStatus() != 412) {
                setFieldValueByObject(account, "正在验证密保问题...","note");
                accountTableView.refresh();
                insertLocalLog(account);
            }
            HttpResponse step212Res = AppleIDUtil.accountRepair(step211Res);
            String XAppleIDSessionId = "";
            String scnt = step212Res.header("scnt");
            List<String> cookies = step212Res.headerList("Set-Cookie");
            for (String item : cookies) {
                if (item.startsWith("aidsp")) {
                    XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
                }
            }
            HttpResponse step213Res = AppleIDUtil.repareOptions(step211Res, step212Res);
            HttpResponse step214Res = AppleIDUtil.securityUpgrade(step213Res, XAppleIDSessionId, scnt);
            HttpResponse step215Res = AppleIDUtil.securityUpgradeSetuplater(step214Res, XAppleIDSessionId, scnt);
            HttpResponse step216Res = AppleIDUtil.repareOptionsSecond(step215Res, XAppleIDSessionId, scnt);
            HttpResponse step22Res = AppleIDUtil.repareComplete(step216Res, step211Res);
            setFieldValueByObject(account, "登录成功","note");
            accountTableView.refresh();
            queryOrUpdate(account, step22Res);
        }else if ("hsa2".equals(authType)) {
            setFieldValueByObject(account, "该账户为双重认证模式，请清空密保信息后重试","note");
            accountTableView.refresh();
            insertLocalLog(account);
        }
        return true;
    }
    private void queryFail(T account,Button accoutQueryBtn,TableView accountTableView) throws Exception {
        String note = "查询失败，请确认用户名密码是否正确";
        account=setFieldValueByObject(account, note,"note");
        accountTableView.refresh();
    }
    /**
    　* 反射获取属性值
      * @param
     * @param object
     * @param targetFieldName
    　* @return java.lang.Object
    　* @throws
    　* @author DeZh
    　* @date 2023/10/26 23:04
    */
    private Object getFieldValueByObject(T object, String targetFieldName) throws Exception {
        // 获取该对象的Class
         Class objClass = object.getClass();
         // 获取所有的属性数组
         Field[] fields = objClass.getDeclaredFields();
         for (Field field:fields) {
             // 属性名称
             String currentFieldName = field.getName();
             if(currentFieldName.equals(targetFieldName)){
                 field.setAccessible(true);
                 Class<?> fieldType = field.getType();
                 Object value=null;
                 if (fieldType == SimpleStringProperty.class) {
                     value=((SimpleStringProperty)field.get(object)).getValue();
                 } else if (fieldType == SimpleIntegerProperty.class) {
                     value=((SimpleIntegerProperty)field.get(object)).getValue();
                 }else{
                     field.get(object);
                 }
                 return value;
             }

        }
        return null;
    }
   /**
   　* 反射设置属性值
     * @param
    * @param object
    * @param value
    * @param targetFieldName
   　* @return T
   　* @throws
   　* @author DeZh
   　* @date 2023/10/26 23:04
   */
    private T setFieldValueByObject(T object, Object value,String targetFieldName) throws Exception {
        // 获取该对象的Class
         Class objClass = object.getClass();
         // 获取所有的属性数组
         Field[] fields = objClass.getDeclaredFields();
         for (Field field:fields) {
             // 属性名称
             String currentFieldName = field.getName();
             if(currentFieldName.equals(targetFieldName)){
                 field.setAccessible(true);
                 field.set(object,new SimpleStringProperty(value.toString()));
                 return object;
             }
         }
         return object;
    }
}

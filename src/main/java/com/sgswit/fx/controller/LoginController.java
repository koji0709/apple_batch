package com.sgswit.fx.controller;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import com.sgswit.fx.controller.base.CommonView;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.HostServicesUtil;
import com.sgswit.fx.utils.MD5Util;
import com.sgswit.fx.utils.StageUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.net.UnknownServiceException;
import java.util.ResourceBundle;

/**
 * 登录controller
 */
public class LoginController extends CommonView implements Initializable {

    @FXML
    private TextField loginUserNameTextField;

    @FXML
    private TextField loginPwdTextField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private CheckBox autoLoginCheckBox;

    @FXML
    private ChoiceBox qqChiceBox;

    @FXML
    private TextField registerUserNameTextField;

    @FXML
    private TextField registerPwdTextField;

    @FXML
    private TextField registerEmailTextField;

    @FXML
    private TextField registerQQTextField;

    @FXML
    private TextField registerCardNoTextField;

    @FXML
    private TextField editUserNameTextField;

    @FXML
    private TextField verifyCodeTextField;

    @FXML
    private TextField newPwdTextField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Setting loginSetting = new Setting("login.setting");

        // 检测是否开启服务, 如果没有开启就直接到主页面方便测试
        Boolean serviceEnable = loginSetting.getBool("service.enable", true);
        if (!serviceEnable){
            StageUtil.show(StageEnum.MAIN);
            return;
        }

        // 记住我
        Boolean rememberMe = loginSetting.getBool("login.rememberMe",false);
        if (rememberMe){
            loginUserNameTextField.setText(loginSetting.getStr("login.userName"));
            loginPwdTextField.setText(loginSetting.getStr("login.pwd"));
            rememberMeCheckBox.setSelected(true);
        }

        // 自动登陆
        Boolean autoLogin = loginSetting.getBool("login.auto",false);
        if (autoLogin){
            autoLoginCheckBox.setSelected(true);
            login();
        }

        // 在线qq
//        if (!autoLogin){
//            ObservableList<String> qqList = FXCollections.observableArrayList();
//            HttpResponse rsp = HttpUtil.createGet(spliceURL("/api/data/getQQList")).execute();
//            boolean success = verifyRsp(rsp);
//            if (success){
//                JSONArray qqArr = dataList(rsp);
//                if (qqArr.size() > 0){
//                    qqList.addAll(qqArr.toList(String.class));
//                }
//            }
//            if (qqList.size()>0){
//                qqChiceBox.setItems(qqList);
//                qqChiceBox.setValue(qqList.get(0));
//            }
//        }

    }

    public void login(){
        String userName = loginUserNameTextField.getText();
        String pwd = loginPwdTextField.getText();

        if (StrUtil.isEmpty(userName) || StrUtil.isEmpty(pwd)){
            alert("用户名和密码不能为空！");
            return;
        }
        String body = "{\"userName\":\"%s\",\"pwd\":\"%s\"}";
        body = String.format(body,userName, MD5Util.encrypt(pwd));
        String userInfo = "";
        try{
            HttpRequest req = HttpUtil.createPost(spliceURL("/userInfo/login"))
                    .header("Content-Type", "application/json")
                    .body(body);
            HttpResponse rsp = req.execute();
            boolean verify = verifyRsp(rsp);
            if (!verify){
                alert(message(rsp));
                return;
            }
            userInfo = JSONUtil.toJsonStr(data(rsp));
        }catch (Exception e){
            alert("登录失败，服务异常", Alert.AlertType.ERROR);
            return;
        }

        // todo 记住我,自动登陆
        Boolean rememberMe = rememberMeCheckBox.isSelected();
        Boolean autoLogin= autoLoginCheckBox.isSelected();

        Setting loginSetting = new Setting("login.setting");
        loginSetting.set("login.auto",autoLogin.toString());
        loginSetting.set("login.rememberMe",rememberMe.toString());
        loginSetting.set("login.userName",userName);
        loginSetting.set("login.pwd",pwd);
        loginSetting.set("login.info",userInfo);
        loginSetting.store(new ClassPathResource("login.setting").getAbsolutePath());

        StageUtil.show(StageEnum.MAIN);
        // 将登陆页面设置为透明,然后关闭
        Stage stage = StageUtil.get(StageEnum.LOGIN);
        stage.setOpacity(0);
        stage.setMaxWidth(0);
        stage.setMinHeight(0);
        StageUtil.close(StageEnum.LOGIN);
    }

    public void qqLogin(){
        String qq = qqChiceBox.getValue().toString();
        if (StrUtil.isEmpty(qq)){
            alert("选中QQ不能为空！");
            return;
        }

        String body = "{\"qq\":\"%s\"}";
        body = String.format(body,qq);
        HttpRequest req = HttpUtil.createPost(spliceURL("/userInfo/qqLogin"))
                .header("Content-Type", "application/json")
                .body(body);
        HttpResponse rsp = req.execute();
        boolean verify = verifyRsp(rsp);
        if (!verify){
            alert(message(rsp));
            return;
        }

        Setting loginSetting = new Setting("login.setting");
        loginSetting.set("login.info",JSONUtil.toJsonStr(data(rsp)));
        loginSetting.store(new ClassPathResource("login.setting").getAbsolutePath());

        Console.log("当前登陆用户：{}",data(rsp).getStr("userName"));
        StageUtil.show(StageEnum.MAIN);
        StageUtil.close(StageEnum.LOGIN);
    }

    public void register(){
        String userName = registerUserNameTextField.getText();
        String pwd = registerPwdTextField.getText();
        String email = registerEmailTextField.getText();
        String qq = registerQQTextField.getText();
        String cardNo = registerCardNoTextField.getText();
        if (StrUtil.isEmpty(userName)){
            alert("注册账号不能为空！");
            return;
        }
        if (StrUtil.isEmpty(pwd)){
            alert("注册密码不能为空！");
            return;
        }
        if (StrUtil.isEmpty(email)){
            alert("安全邮箱不能为空！");
            return;
        }
        if (StrUtil.isEmpty(qq)){
            alert("绑定QQ不能为空！");
            return;
        }

        String body = "{\"userName\":\"%s\",\"pwd\":\"%s\",\"email\":\"%s\",\"qq\":\"%s\",\"cardNo\":\"%s\"}";
        body = String.format(body,userName,MD5Util.encrypt(pwd),email,qq,cardNo);
        HttpRequest req = HttpUtil.createPost(spliceURL("/userInfo/register"))
                .header("Content-Type", "application/json")
                .body(body);
        HttpResponse rsp = req.execute();
        alert(message(rsp));
    }

    public void sendVerifyCode(){
        String userName = editUserNameTextField.getText();
        if (StrUtil.isEmpty(userName)){
            alert("账号不能为空");
            return;
        }
        String body = "{\"userName\":\"%s\"}";
        body = String.format(body,userName);
        HttpRequest req = HttpUtil.createPost(spliceURL("/userInfo/updatePwd/verifyCode"))
                .header("Content-Type", "application/json")
                .body(body);
        HttpResponse rsp = req.execute();
        alert(message(rsp));
    }

    public void updatePwd(){
        String userName = editUserNameTextField.getText();
        String verifyCode = verifyCodeTextField.getText();
        String newPwd = newPwdTextField.getText();

        if (StrUtil.isEmpty(userName)){
            alert("账号不能为空");
            return;
        }
        if (StrUtil.isEmpty(verifyCode)){
            alert("验证码不能为空");
            return;
        }
        if (StrUtil.isEmpty(newPwd)){
            alert("新密码不能为空");
            return;
        }
        String body = "{\"userName\":\"%s\",\"newPwd\":\"%s\",\"verifyCode\":\"%s\"}";
        body = String.format(body,userName,MD5Util.encrypt(newPwd),verifyCode);
        HttpRequest req = HttpUtil.createPost(spliceURL("/userInfo/updatePwd"))
                .header("Content-Type", "application/json")
                .body(body);
        HttpResponse rsp = req.execute();
        alert(message(rsp));
    }

    public void showDocument(){
        String url = "https://qm.qq.com/q/soD5j3tlEk";
        HostServicesUtil.getHostServices().showDocument(url); // 在默认
    }

    public String spliceURL(String api){
        Setting loginSetting = new Setting("login.setting");
        return loginSetting.getStr("service.url") + api;
    }

    public boolean verifyRsp(HttpResponse rsp){
        return rsp.getStatus() == 200 && "200".equals(JSONUtil.parse(rsp.body()).getByPath("code",String.class));
    }

    public String message(HttpResponse rsp){
        if (rsp.getStatus() != 200){
            return "系统异常！";
        }
        return JSONUtil.parse(rsp.body()).getByPath("msg",String.class);
    }

    public JSONObject data(HttpResponse rsp){
        return JSONUtil.parse(rsp.body()).getByPath("data", JSONObject.class);
    }

    public JSONArray dataList(HttpResponse rsp){
        return JSONUtil.parse(rsp.body()).getByPath("data", JSONArray.class);
    }

}

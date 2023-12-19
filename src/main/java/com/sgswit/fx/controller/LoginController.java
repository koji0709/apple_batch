package com.sgswit.fx.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
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
    private ChoiceBox<String> qqChiceBox;

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
        // 检测是否开启服务, 如果没有开启就直接到主页面方便测试
        Setting config = new Setting("config.properties");
        // 检测是否开启服务, 如果没有开启就直接到主页面方便测试
        Boolean serviceEnable = config.getBool("service.enable", true);
        if (!serviceEnable){
            StageUtil.show(StageEnum.MAIN);
            return;
        }
        // 记住我
        Boolean rememberMe = PropertiesUtil.getOtherBool("login.rememberMe",false);
        if (rememberMe){
            loginUserNameTextField.setText(PropertiesUtil.getOtherConfig("login.userName"));
            loginPwdTextField.setText(PropertiesUtil.getOtherConfig("login.pwd"));
            rememberMeCheckBox.setSelected(true);
        }

        // 自动登陆
        Boolean autoLogin = PropertiesUtil.getOtherBool("login.auto",false);
        if (autoLogin){
            autoLoginCheckBox.setSelected(true);

            new Thread(new Runnable() {
                @Override
                public void run(){
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        //JavaFX Application Thread会逐个阻塞的执行这些任务
                        Platform.runLater(new Task<Integer>() {
                            @Override
                            protected Integer call() {
                                login();
                                return 1;
                            }
                        });
                    }
                }
            }).start();
        }

        // 在线qq
        if (!autoLogin){
            ObservableList<String> qqList = FXCollections.observableArrayList();
            for(String qq:TencentQQUtil.getLoginQQList()){
                qqList.add(qq);
            }
            if (qqList.size()>0){
                qqChiceBox.setItems(qqList);
                qqChiceBox.setValue(qqList.get(0));
            }
        }

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
            HttpResponse rsp = HttpUtil.post("/userInfo/login", body);
            boolean verify = HttpUtil.verifyRsp(rsp);
            if (!verify){
                alert(HttpUtil.message(rsp));
                return;
            }
            userInfo = JSONUtil.toJsonStr(HttpUtil.data(rsp));
        }catch (Exception e){
            alert("登录失败，服务异常", Alert.AlertType.ERROR);
            return;
        }

        Boolean rememberMe = rememberMeCheckBox.isSelected();
        Boolean autoLogin= autoLoginCheckBox.isSelected();
//
        PropertiesUtil.setOtherConfig("login.auto",autoLogin.toString());
        PropertiesUtil.setOtherConfig("login.rememberMe",rememberMe.toString());
        PropertiesUtil.setOtherConfig("login.userName",userName);
        PropertiesUtil.setOtherConfig("login.pwd",pwd);
        PropertiesUtil.setOtherConfig("login.info", Base64.encode(userInfo));

        StageUtil.show(StageEnum.MAIN);
        // 将登陆页面设置为透明,然后关闭
        Stage stage = StageUtil.get(StageEnum.LOGIN);
        stage.setOpacity(0);
        stage.setMaxWidth(0);
        stage.setMinHeight(0);
        StageUtil.close(StageEnum.LOGIN);
    }

    public void qqLogin(){
        String qq = qqChiceBox.getValue();
        if (StrUtil.isEmpty(qq)){
            alert("选中QQ不能为空！");
            return;
        }

        String body = "{\"qq\":\"%s\"}";
        body = String.format(body,qq);
        String userInfo ="";
        try {
            HttpResponse rsp = HttpUtil.post("/userInfo/qqLogin", body);
            boolean verify = HttpUtil.verifyRsp(rsp);
            if (!verify){
                alert(HttpUtil.message(rsp));
                return;
            }
            userInfo = JSONUtil.toJsonStr(HttpUtil.data(rsp));
        }catch (Exception e){
            alert("登录失败，服务异常", Alert.AlertType.ERROR);
            return;
        }
        PropertiesUtil.setOtherConfig("login.info", Base64.encode(userInfo));
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
        HttpResponse rsp = HttpUtil.post("/userInfo/register", body);
        alert(HttpUtil.message(rsp));
    }

    public void sendVerifyCode(){
        String userName = editUserNameTextField.getText();
        if (StrUtil.isEmpty(userName)){
            alert("账号不能为空");
            return;
        }
        String body = "{\"userName\":\"%s\"}";
        body = String.format(body,userName);
        HttpResponse rsp = HttpUtil.post("/userInfo/updatePwd/verifyCode", body);
        alert(HttpUtil.message(rsp));
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
        HttpResponse rsp = HttpUtil.post("/userInfo/updatePwd", body);
        alert(HttpUtil.message(rsp));
    }

    public void showDocument() throws IOException {
        Desktop.getDesktop().browse(URI.create("tencent://message/?uin=1215489895"));
    }

}

package com.sgswit.fx.controller;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ReUtil;
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

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
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
            String base64UserName=PropertiesUtil.getOtherConfig("login.userName");
            String base64Pwd=PropertiesUtil.getOtherConfig("login.pwd");
            loginUserNameTextField.setText(SM4Util.decryptBase64(base64UserName));
            loginPwdTextField.setText(SM4Util.decryptBase64(base64Pwd));
            rememberMeCheckBox.setSelected(true);
        }
        Task<Void> loadChartTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // 模拟加载过程
                for (int i = 0; i <= 100; i++) {
                    updateProgress(i, 100);
                    Thread.sleep(50);
                }

                return null;
            }
        };
        // 自动登录
        Boolean autoLogin = PropertiesUtil.getOtherBool("login.auto",false);
        if (autoLogin){
            autoLoginCheckBox.setSelected(true);
            String userName=PropertiesUtil.getOtherConfig("login.userName");
            String pwd=PropertiesUtil.getOtherConfig("login.pwd");
            if (StrUtil.isEmpty(userName) || StrUtil.isEmpty(pwd)){
                return;
            }
            ThreadUtil.execAsync(() -> {
                try {
                    Thread.sleep(1500);
                    //JavaFX Application Thread会逐个阻塞的执行这些任务
                    Platform.runLater(new Task<Integer>() {
                        @Override
                        protected Integer call() {
                            login();
                            return 1;
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        // 在线qq
        if (!autoLogin && SystemUtils.isWindows()){
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
        Boolean rememberMe = rememberMeCheckBox.isSelected();
        Boolean autoLogin= autoLoginCheckBox.isSelected();
        PropertiesUtil.setOtherConfig("login.auto",autoLogin.toString());
        PropertiesUtil.setOtherConfig("login.rememberMe",rememberMe.toString());
        PropertiesUtil.setOtherConfig("login.userName",SM4Util.encryptBase64(userName));
        PropertiesUtil.setOtherConfig("login.pwd",SM4Util.encryptBase64(pwd));
        //利用hutool工具类中的封装方法获取本机mac地址
        String macAddress ="";
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            macAddress= NetUtil.getMacAddress(inetAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String body = "{\"userName\":\"%s\",\"pwd\":\"%s\",\"macAddress\":\"%s\"}";
        body = String.format(body,userName, pwd,macAddress);
        String userInfo;
        try{
            HttpResponse rsp = HttpUtils.post("/userInfo/login", body);
            boolean verify = HttpUtils.verifyRsp(rsp);
            if (!verify){
                alert(HttpUtils.message(rsp),Alert.AlertType.INFORMATION,true);
                return;
            }
            userInfo = JSONUtil.toJsonStr(HttpUtils.data(rsp));
        }catch (Exception e){
            alert("服务异常，请联系管理员", Alert.AlertType.ERROR,true);
            return;
        }


        DataUtil.setUserInfo(userInfo);
        StageUtil.show(StageEnum.MAIN);
        StageUtil.close(StageEnum.LOGIN);
    }

    public void qqLogin(){
        String qq = qqChiceBox.getValue();
        if (StrUtil.isEmpty(qq)){
            alert("选中QQ不能为空！",Alert.AlertType.INFORMATION,true);
            return;
        }

        String body = "{\"qq\":\"%s\"}";
        body = String.format(body,qq);
        String userInfo ="";
        try {
            HttpResponse rsp = HttpUtils.post("/userInfo/qqLogin", body);
            boolean verify = HttpUtils.verifyRsp(rsp);
            if (!verify){
                alert(HttpUtils.message(rsp),Alert.AlertType.INFORMATION,true);
                return;
            }
            userInfo = JSONUtil.toJsonStr(HttpUtils.data(rsp));
        }catch (Exception e){
            alert("登录失败，服务异常", Alert.AlertType.ERROR,true);
            return;
        }
        DataUtil.setUserInfo(userInfo);
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
            alert("注册账号不能为空！",Alert.AlertType.INFORMATION,true);
            return;
        }else{
            String regex = "^[a-zA-Z0-9]{8,20}$";
            if(!userName.matches(regex)){
                alert("账号长度为8到20位,必须包含字母或数字！",Alert.AlertType.INFORMATION,true);
                return;
            }
        }
        if (StrUtil.isEmpty(pwd)){
            alert("注册密码不能为空！",Alert.AlertType.INFORMATION,true);
            return;
        }else{
            String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";
            if(!pwd.matches(pattern)){
                alert("密码长度为8到20位,必须包含大小写字母数字及特殊字符！",Alert.AlertType.INFORMATION,true);
                return;
            }
        }
        if (StrUtil.isEmpty(email)){
            alert("安全邮箱不能为空！",Alert.AlertType.INFORMATION,true);
            return;
        }else{
            if(!Validator.isEmail(email)){
                alert("邮箱格式不正确！",Alert.AlertType.INFORMATION,true);
                return;
            }
        }
        if (StrUtil.isEmpty(qq)){
            alert("绑定QQ不能为空！",Alert.AlertType.INFORMATION,true);
            return;
        }else {
            String QQ_PATTERN = "^[1-9][0-9]{4,10}$";
            if(!ReUtil.isMatch(QQ_PATTERN, qq)){
                alert("QQ格式不正确！",Alert.AlertType.INFORMATION,true);
                return;
            }
        }

        String body = "{\"userName\":\"%s\",\"pwd\":\"%s\",\"email\":\"%s\",\"qq\":\"%s\",\"cardNo\":\"%s\"}";
        body = String.format(body,userName,pwd,email,qq,cardNo);
        HttpResponse rsp = HttpUtils.post("/userInfo/register", body);
        alert(HttpUtils.message(rsp),Alert.AlertType.INFORMATION,true);
    }

    public void sendVerifyCode(){
        String userName = editUserNameTextField.getText();
        if (StrUtil.isEmpty(userName)){
            alert("账号不能为空",Alert.AlertType.INFORMATION,true);
            return;
        }
        String body = "{\"userName\":\"%s\"}";
        body = String.format(body,userName);
        HttpResponse rsp = HttpUtils.post("/userInfo/updatePwd/verifyCode", body);
        alert(HttpUtils.message(rsp),Alert.AlertType.ERROR,true);
    }

    public void updatePwd(){
        String userName = editUserNameTextField.getText();
        String verifyCode = verifyCodeTextField.getText();
        String newPwd = newPwdTextField.getText();

        if (StrUtil.isEmpty(userName)){
            alert("账号不能为空",Alert.AlertType.INFORMATION,true);
            return;
        }
        if (StrUtil.isEmpty(verifyCode)){
            alert("验证码不能为空",Alert.AlertType.INFORMATION,true);
            return;
        }
        if (StrUtil.isEmpty(newPwd)){
            alert("新密码不能为空",Alert.AlertType.INFORMATION,true);
            return;
        }else{
            String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";
            if(!newPwd.matches(pattern)){
                alert("密码长度为8到20位,必须包含大小写字母数字及特殊字符！",Alert.AlertType.INFORMATION,true);
                return;
            }
        }
        String body = "{\"userName\":\"%s\",\"newPwd\":\"%s\",\"verifyCode\":\"%s\"}";
        body = String.format(body,userName,newPwd,verifyCode);
        HttpResponse rsp = HttpUtils.post("/userInfo/updatePwd", body);
        alert(HttpUtils.message(rsp),Alert.AlertType.ERROR,true);
    }

    public void showDocument() throws IOException {
        String customerServiceQQ= PropertiesUtil.getConfig("customer.service.qq");
        Desktop.getDesktop().browse(URI.create("tencent://message/?uin="+customerServiceQQ));
    }

}

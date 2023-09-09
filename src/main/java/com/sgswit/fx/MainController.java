package com.sgswit.fx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author DeZh
 */
public class MainController implements Initializable {

    @FXML
    private ChoiceBox<String> loginMode=new ChoiceBox<>();
    @FXML
    private CheckBox isAutoLogin;
    private final String[] loginModeArr = { "本地登录", "远程服务登录" };



    /**
    　* @description:初始化页面数据
      * @param
     * @param arg0
     * @param arg1
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/9/7 15:01
    */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        //初始化登录模式
        loginMode.getItems().addAll(loginModeArr);
        int index=0;
        //从本地配置文件获取设置的登录模式
        index=Arrays.asList(loginModeArr).indexOf("本地登录");
        loginMode.getSelectionModel().select(index);
        //初始化是否自动登录
        isAutoLogin.setSelected(false);
        //设置监听
        loginModeListener();
        isAutoLoginModeListener();
    }

    /**登录模式监听*/
    protected void loginModeListener(){
        loginMode.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("提示");
                String msg= MessageFormat.format("{0}修改成功，配置已生效！",loginModeArr[Integer.valueOf(t1.toString())]);
                alert.setHeaderText(msg);
                alert.showAndWait();
                //修改本地配置文件
            }
        });

    }
    /**是否自动登录监听*/
    protected void isAutoLoginModeListener(){
        isAutoLogin.selectedProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("提示");
                String msg="";
                if((boolean)t1){
                    msg= "已开启自动登录模式，下次登录时将自动登录软件！";
                }else{
                    msg= "已关闭自动登录模式，下次登录时将执行手动登录！";
                }
                alert.setHeaderText(msg);
                alert.showAndWait();
                //修改本地配置文件
            }
        });

    }
   /**在线购买**/
    @FXML
    protected void onLineBuyBtnClick(ActionEvent actionEvent) throws IOException, URISyntaxException {
        String url = "https://www.baidu.com";
        browse(url);
    }
    private static void browse(String url) {
        try {
            String osName = System.getProperty("os.name", "");
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                openURL.invoke(null, url);
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                // Unix or Linux
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++){
                    // 执行代码，在brower有值后跳出，
                    // 这里是如果进程创建成功了，==0是表示正常结束。
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0){
                        browser = browsers[count];
                    }
                }
                if(browser == null) {
                    throw new Exception("Could not find web browser");
                }else{
                    // 这个值在上面已经成功的得到了一个进程。
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void onIpProxyAndThreadSettingsBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("account-querylog-popup.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 950, 550);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("账户查询记录");

        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
    /**打开自助充值页面**/
    @FXML
    protected void onSelfServiceTopUpBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("account-querylog-popup.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 950, 550);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("账户查询记录");

        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
    @FXML
    protected void callQQ(){
//        String url = "http://wpa.qq.com/msgrd?v=3&uin=748895431&site=qq&menu=yes";
        String url = "https://wpa.qq.com/msgrd?v=3&uin=748895431&site=qq&menu=yes&jumpflag=1";
        browse(url);
    }
}

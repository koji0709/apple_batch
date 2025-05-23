package com.sgswit.fx;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.sgswit.fx.utils.ProcessChecker;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.*;
import com.sgswit.fx.utils.db.SQLiteUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author DELL
 */
public class MainApplication extends Application {
    static {
        //隐式退出开关，设置关闭所有窗口后程序仍不退出
        Platform.setImplicitExit(false);
        // JDK 8u111版本后，目标页面为HTTPS协议，启用proxy用户密码鉴权
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
    }
    @Override
    public void start(Stage stage) throws IOException {
        LoggerManger.info("启动软件...");
        HostServicesUtil.setHostServices(getHostServices());
        DataUtil.getData();
        //进程锁
        FileLock lock = AppleBatchUtil.getLock();

        // 如果获取锁失败，说明程序已经在运行
        if (lock == null) {
            CommonView.alert("对不起，本程序仅允许运行1个!",Alert.AlertType.ERROR);
            return;
        }
        //检测是否开启了抓包工具
        try {
            ProcessChecker.startTimer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 初始化数据库
        try {
            SQLiteUtil.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 添加守护线程，程序退出时释放锁
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                lock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        // 检查并更新版本
        try {
            boolean debug=PropertiesUtil.getConfigBool("debug",true);
            if(!debug && checkAndUpdateVersion()){

            }else{
                StageUtil.show(StageEnum.LOGIN);
                //ToastUtil.init(getHostServices());  // 初始化 HostServices
                //ToastUtil.show("小蓝鲸 - 新版本 1.2.3 可用！", "http://47.121.200.12:15000/api/version/getInfo");
            }
        }catch (Exception e){
            StageUtil.show(StageEnum.LOGIN);
        }
    }
    public boolean checkAndUpdateVersion(){
        //1-windows,2-mac
        String platform=PropertiesUtil.getConfig("softwareInfo.platform");
        String softwareInfoName=PropertiesUtil.getConfig("softwareInfo.name");

        // 查询最新发布的版本信息
        HttpResponse rsp = HttpUtils.get("/api/version/getLastInfo/"+platform);
        boolean success = HttpUtils.verifyRsp(rsp);
        if (!success){
            return false;
        }
        JSONObject versionData = HttpUtils.data(rsp);
        if (versionData == null){
            return false;
        }
        String latestVersionNum = versionData.getStr("versionNum");
        String currentVersionNum = PropertiesUtil.getConfig("softwareInfo.version");
        // 比较版本大小
        if (compareVersion(latestVersionNum,currentVersionNum)<=0){
            return false;
        }
        Map<String,Object> userData=new HashMap<>();
        String name= MessageFormat.format("{0}-Apple批量处理{1}.{2}", new String[]{softwareInfoName,versionData.getStr("version"), "1".equals(platform)?"exe":"dmg"});
        userData.put("name",name);
        userData.put("url",versionData.getStr("url"));

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(StageEnum.UPGRADER.getView()));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), StageEnum.UPGRADER.getHight(), StageEnum.UPGRADER.getWidth());
        } catch (IOException e) {
            e.printStackTrace();
        }
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle(StageEnum.UPGRADER.getTitle());
        popupStage.initModality(StageEnum.UPGRADER.getInitModality());
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageEnum.UPGRADER.getInitStyle());
        String logImg=PropertiesUtil.getConfig("softwareInfo.log.path");
        popupStage.getIcons().add(new Image(logImg) );
        popupStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        UpgraderController upgraderController = fxmlLoader.getController();
        upgraderController.setData(userData);
        upgraderController.startDownload();
        popupStage.show();
        return true;
    }

    public static int compareVersion(String v1, String v2) {
        if (v1.equals(v2)) {
            return 0;
        }
        String[] version1Array = v1.split("[._]");
        String[] version2Array = v2.split("[._]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }
    @Override
    public void init() throws Exception {
        super.init();
    }
    @Override
    public void stop() throws Exception {
        //退出程序
        StageUtil.clearAll();
        Platform.exit();
        System.exit(0);
        super.stop();
    }

    public static void main(String[] args){
        System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        launch();
    }
}

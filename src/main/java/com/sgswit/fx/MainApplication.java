package com.sgswit.fx;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author DELL
 */
public class MainApplication extends Application {

    static {
        //隐式退出开关，设置关闭所有窗口后程序仍不退出
        Platform.setImplicitExit(false);
    }
    @Override
    public void start(Stage stage) throws IOException {
        HostServicesUtil.setHostServices(getHostServices());
        getData();
        //进程锁
        FileLock lock = FileChannel.open(
                Paths.get(System.getProperty("user.dir"), "single_instance.lock"),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE)
                .tryLock();

        // 如果获取锁失败，说明程序已经在运行
        if (lock == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("提示信息");
            alert.setHeaderText(null);
            alert.setContentText("对不起，本程序仅允许运行1个!");
            alert.show();
            return;
        }

        // 初始化数据库
        try {
            SQLiteUtil.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        ProcessBuilder processBuilder = new ProcessBuilder("tasklist.exe");
//        Process process = processBuilder.start();
//        String tasksList = toString(process.getInputStream());
//
//        System.out.println(tasksList);
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
                return;
            }else{
                StageUtil.show(StageEnum.LOGIN);
            }
        }catch (Exception e){
            StageUtil.show(StageEnum.LOGIN);
        }

    }


    private static String toString(InputStream inputStream)
    {
        Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
        String string = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        return string;
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
        String name= MessageFormat.format("{0}-Apple批量处理{1}.{2}", new String[]{softwareInfoName,versionData.getStr("version"),platform.equals("1")?"exe":"dgm"});
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


    protected static void getData(){
        new Thread(() -> DataUtil.getCountry()).start();
        new Thread(() -> DataUtil.getNews()).start();
        new Thread(() -> PointUtil.getPointConfig()).start();
        new Thread(() -> DataUtil.getProxyConfig()).start();
    }
    @Override
    public void init() throws Exception {
        super.init();
    }
    @Override
    public void stop() throws Exception {
        //退出程序
        StageUtil.clearAll();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}

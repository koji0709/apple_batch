package com.sgswit.fx;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.system.SystemUtil;
import com.sgswit.fx.controller.common.CommCodePopupView;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.*;
import com.sgswit.fx.utils.machineInfo.MachineInfoBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class MainApplication extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        HostServicesUtil.setHostServices(getHostServices());
        new Thread(() -> DataUtil.getCountry()).start();
        new Thread(() -> DataUtil.getNews()).start();
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
            alert.setHeaderText("对不起，本程序仅允许运行1个");
            alert.show();
            return;
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
//            if(checkAndUpdateVersion()){
//                return;
//            }else{
                StageUtil.show(StageEnum.LOGIN);
//            }
        }catch (Exception e){

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

        // 查询最新发布的版本信息
        HttpResponse rsp = HttpUtil.get("/versionControl/latestVersion/"+platform);
        boolean success = HttpUtil.verifyRsp(rsp);
        if (!success){
            return false;
        }
        JSONObject versionData = HttpUtil.data(rsp);
        if (versionData == null){
            return false;
        }
        Double latestVersionNum = versionData.getDouble("versionNum");
        String currentVersionNum = PropertiesUtil.getConfig("softwareInfo.version");

        // 如果没有设置当前版本信息, 则默认最高版本信息
        boolean isNumber = NumberUtil.isNumber(currentVersionNum);
        if (StrUtil.isEmpty(currentVersionNum) || !isNumber){
            PropertiesUtil.setOtherConfig("version",String.valueOf(latestVersionNum));
            return false;
        }

        // 比较版本大小
        if (latestVersionNum <= Double.valueOf(currentVersionNum)){
            return false;
        }

        // 检测是否强制更新
        int isForceUpdate = versionData.getInt("isForceUpdate");

        // 强制更新
        if (isForceUpdate == 0){
            Map<String,Object> userData=new HashMap<>();
            userData.put("name","果果批处理程序");
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
        }else{
            return false;
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
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}

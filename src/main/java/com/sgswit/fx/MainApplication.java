package com.sgswit.fx;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.system.SystemUtil;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.*;
import com.sgswit.fx.utils.machineInfo.MachineInfoBuilder;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        HostServicesUtil.setHostServices(getHostServices());
        new Thread(() -> DataUtil.getCountry()).start();
        new Thread(() -> DataUtil.getNews()).start();
        String guid=PropertiesUtil.getOtherConfig("guid");
        if(StrUtil.isEmpty(guid)){
            guid = MachineInfoBuilder.generateMachineInfo().getMachineGuid();
            PropertiesUtil.setOtherConfig("guid",guid);
        }
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

        // 添加守护线程，程序退出时释放锁
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                lock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        // 检查并更新版本
        checkAndUpdateVersion();
        StageUtil.show(StageEnum.LOGIN);
    }

    public void checkAndUpdateVersion(){
        // 查询最新发布的版本信息
        HttpResponse rsp = HttpUtil.get("/versionControl/latestVersion");
        boolean success = HttpUtil.verifyRsp(rsp);
        if (!success){
            return;
        }
        JSONObject versionData = HttpUtil.data(rsp);
        if (versionData == null){
            return;
        }
        Double latestVersionNum = versionData.getDouble("versionNum");
        String currentVersionNum = PropertiesUtil.getOtherConfig("version");

        // 如果没有设置当前版本信息, 则默认最高版本信息
        boolean isNumber = NumberUtil.isNumber(currentVersionNum);
        if (StrUtil.isEmpty(currentVersionNum) || !isNumber){
            PropertiesUtil.setOtherConfig("version",String.valueOf(latestVersionNum));
            return;
        }

        // 比较版本大小
        if (latestVersionNum <= Double.valueOf(currentVersionNum)){
            return;
        }

        // 检测是否强制更新
        int isForceUpdate = versionData.getInt("isForceUpdate");

        // 强制更新
        if (isForceUpdate == 0){
            // MAC OS X / Windows 11
            String osName = SystemUtil.getOsInfo().getName();
            String downloadUrl = osName.toUpperCase().startsWith("MAC")
                    ? versionData.getStr("macUrl")
                    : versionData.getStr("winUrl");

            // todo 自动更新?
            // 目标地址
            String dest = "/var/tmp/";
            try{
                long contentLenght = cn.hutool.http.HttpUtil.downloadFile(downloadUrl, dest);
                if (contentLenght > 0){
                    // 更新版本信息
                    PropertiesUtil.setOtherConfig("version",String.valueOf(latestVersionNum));
                    return;
                }
            }catch (Exception e){
            }

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

package com.sgswit.fx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.*;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        HostServicesUtil.setHostServices(getHostServices());
        new Thread(() -> DataUtil.getCountry()).start();
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
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}

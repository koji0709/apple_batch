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
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.channels.FileLock;
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
           if( CommonView.confirmationDialog("提示","对不起，本程序仅允许运行1个")){
                //退出程序
               StageUtil.clearAll();
               Platform.exit();
               System.exit(0);
           }
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
        StageUtil.show(StageEnum.LOGIN);
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

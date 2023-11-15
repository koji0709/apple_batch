package com.sgswit.fx;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.HostServicesUtil;
import com.sgswit.fx.utils.PropertiesUtil;
import com.sgswit.fx.utils.StageUtil;
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

        String guid=PropertiesUtil.getOtherConfig("guid");
        if(StrUtil.isEmpty(guid)){
            guid = MachineInfoBuilder.generateMachineInfo().getMachineGuid();
            PropertiesUtil.setOtherConfig("guid",guid);
        }

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
        StageUtil.show(StageEnum.LOGIN);
    }

    @Override
    public void init() throws Exception {
        Console.log("init");
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

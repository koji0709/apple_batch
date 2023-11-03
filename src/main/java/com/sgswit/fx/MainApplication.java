package com.sgswit.fx;

import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.HostServicesUtil;
import com.sgswit.fx.utils.StageUtil;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        HostServicesUtil.setHostServices(getHostServices());
        new Thread(() -> DataUtil.getCountry()).start();
        StageUtil.show(StageEnum.LOGIN);
        //StageUtil.show(StageEnum.MAIN);
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

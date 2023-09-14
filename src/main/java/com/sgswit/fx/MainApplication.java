package com.sgswit.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 520);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        stage.setTitle("欢迎使用APPLE批量处理程序");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        System.out.println("start....");
    }

    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("init....");
    }
    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("stop....");
    }

    public static void main(String[] args) {
        launch();
    }
}

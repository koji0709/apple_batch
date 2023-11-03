package com.sgswit.fx.utils;

import com.sgswit.fx.MainApplication;
import com.sgswit.fx.enums.StageEnum;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StageUtil {

    private static Map<StageEnum, Stage> stageMap = new HashMap<>();

    public static void showAndWait(StageEnum stageEnum){
        show(stageEnum,true);
    }

    public static void show(StageEnum stageEnum){
        show(stageEnum,false);
    }

    public static void show(StageEnum stageEnum,boolean isWait){
        Stage stage = stageMap.get(stageEnum);
        if (stage != null && stage.isShowing()){
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(stageEnum.getView()));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getRoot().setStyle("-fx-font-family: '"+stageEnum.getFontStyle()+"'");
        stage = new Stage();
        stage.setTitle(stageEnum.getTitle());
        stage.initModality(stageEnum.getInitModality());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initStyle(stageEnum.getInitStyle());
        if (isWait){
            stage.showAndWait();
        }else{
            stage.show();
        }
        stageMap.put(stageEnum,stage);
    }


    public static void close(StageEnum stageEnum){
        Stage stage = stageMap.get(stageEnum);
        if (stage != null && stage.isShowing()){
            stage.close();
        }
    }

}

package com.sgswit.fx.utils;

import com.sgswit.fx.MainApplication;
import com.sgswit.fx.enums.StageEnum;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
        stage = new Stage();
        stageMap.put(stageEnum,stage);

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(stageEnum.getView()));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getRoot().setStyle("-fx-font-family: '"+stageEnum.getFontStyle()+"'");
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
        //判断程序是否退出
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent arg0) {
                if((Stage)arg0.getSource()==StageUtil.get(StageEnum.MAIN)){
                    System.exit(0);
                }
            }
        });

    }

    public static void close(StageEnum stageEnum){
        Stage stage = stageMap.get(stageEnum);
        if (stage != null && stage.isShowing()){
            stageMap.remove(stageEnum);
            stage.close();
        }
    }

    public static Stage get(StageEnum stageEnum){
        Stage stage = stageMap.get(stageEnum);
        return stage;
    }
    public static void clearAll(){
        stageMap.clear();
    }

}

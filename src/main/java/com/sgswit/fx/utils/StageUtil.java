package com.sgswit.fx.utils;

import cn.hutool.core.util.ReflectUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.enums.StageEnum;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StageUtil {
    private static Map<StageEnum, Stage> stageMap = new HashMap<>();

    public static void showAndWait(StageEnum stageEnum){
        show(stageEnum,true);
    }

    public static void show(StageEnum stageEnum){
        show(stageEnum,null);
    }

    public static void show(StageEnum stageEnum,Object userData){
        show(stageEnum,false,userData);
    }

    public static void show(StageEnum stageEnum,boolean isWait){
        show(stageEnum,isWait,null);
    }

    public static void show(StageEnum stageEnum,boolean isWait,Object userData){
        Stage stage = stageMap.get(stageEnum);
        if (stage != null && stage.isShowing()){
            //最小化之后，点击显示
            StageToSystemTrayUtil.showWindow(stage);
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


        stage.setUserData(userData);
        stage.initStyle(stageEnum.getInitStyle());
        String logImg=PropertiesUtil.getConfig("softwareInfo.log.path");
        stage.getIcons().add(new Image(logImg) );

        if(stage==StageUtil.get(StageEnum.MAIN)){
            //判断是否支持系统托盘
            if (SystemTray.isSupported()) {
                // 创建系统托盘图标
                StageToSystemTrayUtil.createTrayIcon(stage);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initStyle(StageStyle.UTILITY);
            }else{
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initStyle(StageStyle.DECORATED);
            }
        }else if(stage==StageUtil.get(StageEnum.LOGIN)){
            stage.setAlwaysOnTop(true);
        }
        if (isWait){
            stage.showAndWait();
        }else{
            stage.show();
        }
        //判断程序是否退出
        Stage finalStage = stage;
        stage.setOnCloseRequest(event -> {
            Stage source=(Stage)event.getSource();
            if(source==StageUtil.get(StageEnum.MAIN)){
                if (SystemTray.isSupported()){
                    StageToSystemTrayUtil.hideWindow(finalStage);
                    event.consume();
                }else {
                    System.exit(0);
                }
            }else if((source==StageUtil.get(StageEnum.LOGIN)) && null==StageUtil.get(StageEnum.MAIN)){
                System.exit(0);
            }else{
                try {
                    Object controller = fxmlLoader.getController();
                    Boolean f = ReflectUtil.invoke(controller,"validateData");
                    if(f){
                        event.consume();
                    }
                }catch (Exception e){

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

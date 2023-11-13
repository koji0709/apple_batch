package com.sgswit.fx.controller.tool;

import com.sgswit.fx.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 *
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/27
 */
public class ToolboxltemsController {


    {
        File file = new File("");
        String s = null;
        try {
            s = file.getCanonicalPath() + "\\文本处理\\";
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file1 = new File(s);
        if(!file1.exists()){
            file1.mkdir();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/tool/toolbox.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 1100, 700);
        } catch (IOException e) {
            e.printStackTrace();
        }
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("工具箱");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }


}

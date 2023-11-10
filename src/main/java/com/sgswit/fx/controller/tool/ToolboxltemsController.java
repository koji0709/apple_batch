package com.sgswit.fx.controller.tool;

import com.sgswit.fx.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

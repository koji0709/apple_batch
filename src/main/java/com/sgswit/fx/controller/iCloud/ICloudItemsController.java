package com.sgswit.fx.controller.iCloud;

import com.sgswit.fx.MainApplication;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.StageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * @author DeZh
 * @title: ITunesItemsController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1321:39
 */
public class ICloudItemsController {

    public void onCheckWhetherIcloudBtnClick(ActionEvent actionEvent) throws IOException {
        StageUtil.show(StageEnum.CHECK_WHETHER_ICLOUD);
    }

    public void onFamilyDetailsBtnClick(ActionEvent actionEvent) {
        StageUtil.show(StageEnum.FAMILY_DETAILS);
    }
}

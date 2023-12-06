package com.sgswit.fx.controller.iTunes;

import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.controller.iTunes.vo.AppstoreDownloadVo;

import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.StageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class AppstoreDownloadController extends TableView<AppstoreDownloadVo> {

    @FXML
    CheckBox useUrlCheckBox;

    @FXML
    TextField localUrlTextField;

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView(AppstoreDownloadVo.class,"account----pwd");
    }

    /**
     * 账号处理
     */
    @Override
    public void accountHandler(AppstoreDownloadVo appstoreDownloadVo) {
        System.err.println(appstoreDownloadVo);
    }

    public void showAppstoreSearchStage(){
        StageUtil.show(StageEnum.APPSTORE_SEARCH);
    }

}

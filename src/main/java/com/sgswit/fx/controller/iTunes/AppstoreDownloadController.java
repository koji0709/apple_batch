package com.sgswit.fx.controller.iTunes;

import cn.hutool.json.JSONObject;
import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.controller.iTunes.vo.AppstoreDownloadVo;

import com.sgswit.fx.controller.iTunes.vo.AppstoreItemVo;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.StageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppstoreDownloadController extends TableView<AppstoreDownloadVo> {

    @FXML
    CheckBox useUrlCheckBox;

    @FXML
    TextField localUrlTextField;

    List<AppstoreItemVo> appstoreItemVoList = new ArrayList<>();


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

    /**
     * 从苹果商店添加
     */
    public void showAppstoreSearchStage(){
        Map<String,Object> userData = new HashMap<>();
        userData.put("localUrlTextField",localUrlTextField);
        StageUtil.show(StageEnum.APPSTORE_SEARCH,userData);
    }

    /**
     * 从本地文件添加
     */
    public void addItemFromLocalBtnAction(){
        System.err.println(appstoreItemVoList);
    }

    public void localUrlFileChooseBtnAction(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT", "*.txt")
        );
        File file = fileChooser.showOpenDialog(StageUtil.get(StageEnum.APPSTORE_DOWNLOAD));
        if (file != null){
            localUrlTextField.setText(file.getAbsolutePath());
        }
    }

}

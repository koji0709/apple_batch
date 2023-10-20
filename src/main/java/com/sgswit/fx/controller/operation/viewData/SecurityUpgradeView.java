package com.sgswit.fx.controller.operation.viewData;

import com.sgswit.fx.controller.base.TableView;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 * 开通双重认证view元素
 */
public class SecurityUpgradeView extends TableView {

    /**
     * 随机ip代理
     */
    @FXML
    protected CheckBox randomIPProxyCheckBox;

    /**
     * 手机号码地区
     */
    @FXML
    protected ChoiceBox dialCodeChoiceBox;
}

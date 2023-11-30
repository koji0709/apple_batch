package com.sgswit.fx.controller.operation.viewData;

import com.sgswit.fx.controller.common.AppleIdView;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;

/**
 * 开通双重认证view元素
 */
public class SecurityUpgradeView extends AppleIdView {

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

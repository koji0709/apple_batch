package com.sgswit.fx.controller.operation.viewData;

import com.sgswit.fx.controller.common.AppleIdView;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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
    protected ComboBox dialCodeComboBox;

    /**
     * 是否api接码
     */
    @FXML
    protected CheckBox apiCheckBox;

    /**
     * 延迟查码
     */
    @FXML
    protected TextField apiYcTextField;

    /**
     * 查码次数
     */
    @FXML
    protected TextField apiCsTextField;

    /**
     * 读码配置
     */
    @FXML
    protected TextField apiDmTextField;

    /**
     * url
     */
    @FXML
    protected Label url;
}

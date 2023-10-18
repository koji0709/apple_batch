package com.sgswit.fx.controller.operation.viewData;

import com.sgswit.fx.controller.base.TableView;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

/**
 * 账号解锁改密view元素
 */
public class UnlockChangePasswordView extends TableView {

    /**
     * 随机ip代理
     */
    @FXML
    protected CheckBox randomIPProxyCheckBox;

    /**
     * 新密码
     */
    @FXML
    protected TextField pwdTextField;
}

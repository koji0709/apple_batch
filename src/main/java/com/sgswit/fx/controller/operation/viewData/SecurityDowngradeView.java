package com.sgswit.fx.controller.operation.viewData;

import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.model.Account;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

/**
 * 关闭双重认证view元素
 */
public class SecurityDowngradeView extends CustomTableView<Account> {

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

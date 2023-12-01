package com.sgswit.fx.controller.operation.viewData;

import com.sgswit.fx.controller.common.AppleIdView;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * 苹果更改账号view元素
 */
public class UpdateAppleIDView extends AppleIdView {

    /**
     * 随机ip代理
     */
    @FXML
    protected CheckBox randomIPProxyCheckBox;

    /**
     * 工作模式
     */
    @FXML
    protected ChoiceBox opTypeChoiceBox;

    /**
     * 验证码延时
     */
    @FXML
    protected TextField verifyCodeDelayTextField;

    /**
     * 是否同时修改其他资料
     */
    @FXML
    protected CheckBox updateAccountInfoCheckBox;

    /**
     * 新生日
     */
    @FXML
    protected DatePicker birthdayDatePicker;

    /**
     * 新密码
     */
    @FXML
    protected TextField pwdTextField;

    /**
     * 答案1
     */
    @FXML
    protected TextField answer1TextField;

    /**
     * 答案2
     */
    @FXML
    protected TextField answer2TextField;

    /**
     * 答案3
     */
    @FXML
    protected TextField answer3TextField;

}

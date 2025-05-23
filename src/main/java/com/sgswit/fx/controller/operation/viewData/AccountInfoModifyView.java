package com.sgswit.fx.controller.operation.viewData;

import com.sgswit.fx.controller.common.AppleIdView;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * 官网修改资料view元素
 */
public class AccountInfoModifyView extends AppleIdView {

    /**
     * 修改密码
     */
    @FXML
    protected CheckBox updatePwdCheckBox;

    /**
     * 新密码
     */
    @FXML
    protected TextField pwdTextField;

    /**
     * 修改生日
     */
    @FXML
    protected CheckBox updateBirthdayCheckBox;

    /**
     * 新生日
     */
    @FXML
    protected TextField birthdayTextField;

    /**
     * 修改姓名
     */
    @FXML
    protected CheckBox updateNameCheckBox;

    /**
     * 姓氏
     */
    @FXML
    protected TextField lastNameTextField;

    /**
     * 名字
     */
    @FXML
    protected TextField firstNameTextField;

    /**
     * 修改密保
     */
    @FXML
    protected CheckBox updatePasswordProtectionCheckBox;

    /**
     * 删除设备
     */
    @FXML
    protected CheckBox removeDeviceCheckBox;

    /**
     * 姓名生成方式
     */
    @FXML
    protected ChoiceBox nameGenerationTypeChoiceBox;

    /**
     * 问题1
     */
    @FXML
    protected ChoiceBox question1ChoiceBox;

    /**
     * 问题2
     */
    @FXML
    protected ChoiceBox question2ChoiceBox;

    /**
     * 问题3
     */
    @FXML
    protected ChoiceBox question3ChoiceBox;

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

    /**
     * 移除救援邮箱
     */
    @FXML
    protected CheckBox removeRescueEmailCheckBox;

    /**
     * 修改显示语言
     */
    @FXML
    protected CheckBox updateShowLangCheckBox;

    /**
     * 显示语言
     */
    @FXML
    protected ComboBox updateShowLangComboBox;

}

package com.sgswit.fx.enums;

import javafx.stage.Modality;
import javafx.stage.StageStyle;

public enum StageEnum {
    /********** MAIN区 ***********/
    LOGIN("APPLE批量处理程序 - 登录","views/login.fxml"),
    MAIN("欢迎使用APPLE批量处理程序","views/main-view.fxml",400,520,StageStyle.UNDECORATED,Modality.NONE,""),

    /********** OPERATION区 ***********/
    ACCOUNT_INFO_MODIFY("官方修改资料","views/operation/account-info-modify.fxml",1530,760,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    UNLOCK_CHANGE_PASSWORD("账号解锁改密","views/operation/unlock-change-password.fxml",1260,760,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    UPDATE_APPLE_ID("苹果更改账号","views/operation/update-appleid.fxml",1530,760,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    SECURITY_UPGRADE("开通双重认证","views/operation/security-upgrade.fxml",1260,760,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    SECURITY_DOWNGRADE("关闭双重认证","views/operation/security-downgrade.fxml",1260,760,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    SUPPORT_PIN("生成支持PIN","views/operation/support-pin.fxml",1260,760,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),

    /********** ICLOUD区 ***********/
    CHECK_WHETHER("能否登录iCloud","views/iCloud/check-whether-icloud.fxml",1000,650,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),

    /********** ITUNES区 ***********/
    COUNTRY_MODIFY("账号国家修改","views/iTunes/country-modify.fxml",1100,650,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    DELETE_PAYMENT("删除付款方式","views/iTunes/payment-method.fxml",1100,650,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    GIFTCARD_BLANCE("礼品卡查余额","views/iTunes/giftCard-balance-check.fxml",1100,650,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),


    /********** QUERY区 ***********/
    BIRTHDAY_COUNTRY_QUERY("查询生日国家","views/query/birthday-country.fxml",1100,700,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    BALANCE_QUERY("密保查询余额","views/query/balance-query.fxml",1100,700,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    CHECK_GRAY_BALANCE("检测灰余额","views/query/detection-gray-balance.fxml",1100,700,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    WHETHER_APPLEID("检测是否AppleID","views/query/whether-appleid.fxml",1100,700,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    RAPID_FILTRATION("急速过滤密正","views/query/rapid-filtration.fxml",1100,700,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    DETECTION_WHETHER("检测是否过检","views/query/detection-whether-inspection.fxml",1100,700,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),
    SECURITY_QUESTION("查询密保问题","views/query/security-question-query.fxml",1100,700,StageStyle.UTILITY,Modality.APPLICATION_MODAL,""),

    /********** TOOLBOX区 ***********/

    ;
    private String     title;
    private String     view;
    private Integer    width;
    private Integer    hight;
    private StageStyle initStyle   = StageStyle.UTILITY;
    private Modality   initModality = Modality.WINDOW_MODAL;
    private String     fontStyle    = "serif";

    StageEnum(String title, String view) {
        this.title = title;
        this.view = view;
    }

    StageEnum(String title, String view, Integer width, Integer hight) {
        this.title = title;
        this.view = view;
        this.width = width;
        this.hight = hight;
    }

    StageEnum(String title, String view, Integer width, Integer hight, StageStyle initStyle, Modality initModality, String fontStyle) {
        this.title = title;
        this.view = view;
        this.width = width;
        this.hight = hight;
        this.initStyle = initStyle;
        this.initModality = initModality;
        this.fontStyle = fontStyle;
    }

    public String getView() {
        return view;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHight() {
        return hight;
    }

    public String getTitle() {
        return title;
    }

    public StageStyle getInitStyle() {
        return initStyle;
    }

    public Modality getInitModality() {
        return initModality;
    }

    public String getFontStyle() {
        return fontStyle;
    }
}

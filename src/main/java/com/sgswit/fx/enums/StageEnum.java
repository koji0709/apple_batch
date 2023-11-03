package com.sgswit.fx.enums;

import javafx.stage.Modality;
import javafx.stage.StageStyle;

public enum StageEnum {
    LOGIN("APPLE批量处理程序 - 登陆","views/login.fxml"),
    MAIN("欢迎使用APPLE批量处理程序","views/main-view.fxml",400,520),
    ACCOUNT_INFO_MODIFY("官方修改资料","views/operation/account-info-modify.fxml",1530,760),
    UNLOCK_CHANGE_PASSWORD("账号解锁改密","views/operation/unlock-change-password.fxml",1260,760),
    UPDATE_APPLE_ID("苹果更改账号","views/operation/update-appleid.fxml",1530,760),
    SECURITY_UPGRADE("开通双重认证","views/operation/security-upgrade.fxml",1260,760),
    SECURITY_DOWNGRADE("关闭双重认证","views/operation/security-downgrade.fxml",1260,760),
    SUPPORT_PIN("生成支持PIN","views/operation/support-pin.fxml",1260,760),

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

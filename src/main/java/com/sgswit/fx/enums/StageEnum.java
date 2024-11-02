package com.sgswit.fx.enums;

import com.sgswit.fx.utils.PropertiesUtil;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

public enum StageEnum {
    /********** 升级 ***********/
    UPGRADER(PropertiesUtil.getConfig("softwareInfo.name")+"-自动更新","views/upgrader.fxml",400,600,StageStyle.DECORATED,Modality.APPLICATION_MODAL,""),
    /********** MAIN区 ***********/
    LOGIN(PropertiesUtil.getConfig("softwareInfo.name")+" APPLE批量处理 - 登录","views/login.fxml",400,400,StageStyle.DECORATED,Modality.APPLICATION_MODAL,""),
    MAIN(PropertiesUtil.getConfig("softwareInfo.name")+" - APPLE批量处理 V"+PropertiesUtil.getConfig("softwareInfo.version"),"views/main-view.fxml",400,520,StageStyle.UTILITY,Modality.NONE,""),
    SELF_SERVICE_CHARGE("自助充值","views/base/selfServiceCharge.fxml",385,170,StageStyle.UTILITY,Modality.WINDOW_MODAL,""),

    /********** OPERATION区 ***********/
    ACCOUNT_INFO_MODIFY("官方修改资料","views/operation/account-info-modify.fxml",1260,650),
    UNLOCK_CHANGE_PASSWORD("账号解锁改密","views/operation/unlock-change-password.fxml",1260,650),
    UPDATE_APPLE_ID("苹果更改账号","views/operation/update-appleid.fxml",1260,650),
    SECURITY_UPGRADE("开通双重认证","views/operation/security-upgrade.fxml",1260,650),
    SECURITY_DOWNGRADE("关闭双重认证","views/operation/security-downgrade.fxml",1260,650),
    SUPPORT_PIN("生成支持PIN","views/operation/support-pin.fxml",1260,650),

    /********** ICLOUD区 ***********/
    CHECK_WHETHER_ICLOUD("能否登录iCloud","views/iCloud/check-whether-icloud.fxml",1000,650),
    FAMILY_DETAILS("家庭共享详细","views/iCloud/family-details.fxml",1000,650),
    DREDGE_FAMILY("开通家庭共享","views/iCloud/dredge-family.fxml",1000,650),
    CLOSE_FAMILY("关闭家庭共享","views/iCloud/close-family.fxml",1000,650),
    FAMILY_MEMBERS("管理家庭成员","views/iCloud/family-members.fxml",1000,650),
    ICLOUD_FUNCTIONAL_TESTING("iCloud功能检测","views/iCloud/icloud-functional-testing.fxml",1000,650),
    ICLOUD_WEB_REPAIR("修复网页iCloud","views/iCloud/icloud-web-repair.fxml",1000,650),
    ICLOUD_ACTIVATE_MAIL("激活iCloud邮箱","views/iCloud/icloud-activate-mail.fxml",1000,650),

    /********** ITUNES区 ***********/
    COUNTRY_MODIFY("账号修改国家","views/iTunes/country-modify.fxml",1100,650),
    DELETE_PAYMENT("删除付款方式","views/iTunes/payment-method.fxml",1100,650),
    BIND_VIRTUAL_CARD("绑定虚拟卡","views/iTunes/bind-virtual-card.fxml",1100,650),
    GIFTCARD_BALANCE("礼品卡查余额","views/iTunes/giftCard-balance-check.fxml",1100,650),
    CHECK_AREA_BALANCE("国家区域余额","views/iTunes/check_area_balance.fxml",1100,650),
    CHECK_BALANCE_DISABLEDSTATUS("查询余额/检测禁用","views/iTunes/check_balance_disabledstatus.fxml",1100,650),
    CONSUMPTION_BILL("查询消费账单","views/iTunes/consumption-bill.fxml",1150,700),
    QUERY_ACCOUNT_INFO("查询账号信息","views/iTunes/account-info.fxml",1150,700),
    APPSTORE_DOWNLOAD("批量下载APP","views/iTunes/appstore_download.fxml"),
    APPSTORE_SEARCH("Appstore搜索","views/iTunes/appstore_search.fxml"),
    GIFTCARD_BATCH_REDEEM("苹果批量兑换","views/iTunes/giftcard_batch_redeem.fxml"),
    GIFTCARD_BATCH_REDEEM2("苹果批量兑换","views/iTunes/giftcard_batch_redeem.fxml"),
    GIFTCARD_BATCH_REDEEM3("苹果批量兑换","views/iTunes/giftcard_batch_redeem.fxml"),
    GIFTCARD_BATCH_PARSE_ACCOUNT("批量ID加卡","views/iTunes/giftcard_batch_parse_account.fxml"),


    GIFTCARD_DETECTION_PROFESSIONAL_EDITION("礼品卡检测专业版","views/iTunes/giftcard_detection_professional_edition.fxml"),
    CHN_APPLE_ID_VALIDATE("中国区ID身份认证","views/iTunes/chn-appleId-validate.fxml"),

    /********** QUERY区 ***********/
    BIRTHDAY_COUNTRY_QUERY("查询生日国家","views/query/birthday-country.fxml",1100,650),
    BALANCE_QUERY("密保查询余额","views/query/balance-query.fxml",1100,700),
    CHECK_GRAY_BALANCE("检测灰余额","views/query/detection-gray-balance.fxml",1100,650),
    WHETHER_APPLEID("检测是否AppleID","views/query/whether-appleid.fxml",1100,650),
    RAPID_FILTRATION("极速过滤密正","views/query/rapid-filtration.fxml",1100,650),
    DETECTION_WHETHER("检测是否过检","views/query/detection-whether-inspection.fxml",1100,650),
    SECURITY_QUESTION("查询密保问题","views/query/security-question-query.fxml",1100,650),

    /********** TOOLBOX区 ***********/

    ;
    private String     title;
    private String     view;
    private Integer    width;
    private Integer    hight;
    private StageStyle initStyle   = StageStyle.DECORATED;
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

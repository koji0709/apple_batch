package com.sgswit.fx.enums;

import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DELL
 */

public enum DataImportEnum {
    /********** OPERATION区 ***********/

    /********** ICLOUD区 ***********/
//    CHECK_WHETHER_ICLOUD("能否登录iCloud","views/iCloud/check-whether-icloud.fxml",1000,650),
//    FAMILY_DETAILS("家庭共享详细","views/iCloud/family-details.fxml",1000,650),

    /********** ITUNES区 ***********/
//    COUNTRY_MODIFY("账号修改国家","views/iTunes/country-modify.fxml",1100,650),
    BIND_VIRTUAL_CARD("account----pwd----creditCardNumber/monthAndYear/creditVerificationNumber",new ArrayList<>(){{
        add("格式为:帐号---密码----卡号/月份年份/安全码。");
        add("一次可以输入多条账户信息，每条账户单独一行;如果数据中有-符号,则使用{-}替换。");
    }}),
    COMM("account----pwd",new ArrayList<>(){{
        add("格式为:账号----密码。");
        add("一次可以输入多条账户信息，每条账户单独一行;如果数据中有-符号,则使用{-}替换。");
    }}),
//    BIND_VIRTUAL_CARD("绑定虚拟卡","views/iTunes/bind-virtual-card.fxml",1100,650),
//    GIFTCARD_BLANCE("礼品卡查余额","views/iTunes/giftCard-balance-check.fxml",1100,650),
//    CHECK_AREA_BALANCE("国家区域余额","views/iTunes/check_area_balance.fxml",1100,650),
//    CHECK_BALANCE_DISABLEDSTATUS("查询余额/检测禁用","views/iTunes/check_balance_disabledstatus.fxml",1100,650),
//    CONSUMPTION_BILL("查询消费账单","views/iTunes/consumption-bill.fxml",1150,700),
//    QUERY_ACCOUNT_INFO("查询账号信息","views/iTunes/account-info.fxml",1150,700),
    /********** QUERY区 ***********/

    /********** TOOLBOX区 ***********/

    ;
    private String format;
    private List<String> description;

    DataImportEnum(String format, List<String> description) {
        this.format = format;
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }
}

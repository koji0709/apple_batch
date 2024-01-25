package com.sgswit.fx.enums;

/**
 * @author DELL
 */

public enum FunctionListEnum {
    /********** ITUNES区 ***********/
    COUNTRY_MODIFY("1110",80,"账号修改国家"),
    CONSUMPTION_BILL("1120",30,"查询消费账单"),
    QUERY_ACCOUNT_INFO("1130",15,"查询账号信息"),
    GIFTCARD_BLANCE("1140",10,"礼品卡查余额"),
    GIFTCARD_BATCH_REDEEM("1150",30,"苹果批量兑换"),
    GIFTCARD_BATCH_REDEEM_QUERY("1151",5,"苹果批量兑换账号检测"),
    GIFTCARD_BATCH_REDEEM_QUERY_PE("1152",30,"礼品卡检测专业版"),
    GIFTCARD_REDEEM_LOG_QUERY("1153",100,"礼品卡大数据"),


    CHECK_AREA_BALANCE("1160",50,"国家区域余额"),
    CHECK_BALANCE_DISABLED_STATUS("1170",5,"查询余额/检测禁用"),
    APPSTORE_DOWNLOAD("1180",30,"批量下载APP"),
    APPSTORE_SEARCH("1190",30,"管理订阅项目"),
    DELETE_PAYMENT("1200",80,"删除付款方式"),
    BIND_VIRTUAL_CARD("1210",30,"绑定虚拟卡"),

    /********** ICLOUD区 ***********/
    CHECK_WHETHER_ICLOUD("2110",5,"能否登录iCloud"),
    FAMILY_DETAILS("2120",15,"家庭共享详细"),
    DREDGE_FAMILY("2130",40,"开通家庭共享"),
    CLOSE_FAMILY("2140",20,"关闭家庭共享"),
    FAMILY_MEMBERS("2150",30,"管理家庭成员"),
    ICLOUD_FUNCTIONAL_TESTING("2160",10,"iCloud功能检测"),
    ICLOUD_WEB_REPAIR("2170",40,"修复网页iCloud"),
    ICLOUD_ACTIVATE_MAIL("2180",60,"激活iCloud邮箱"),
    /********** OPERATION区 ***********/
    ACCOUNT_INFO_MODIFY("3110",30,"官方修改资料"),
    UNLOCK_CHANGE_PASSWORD("3120",20,"账号解锁改密"),
    UPDATE_APPLE_ID("3130",20,"苹果更改账号"),
    SECURITY_UPGRADE("3140",20,"开通双重认证"),
    SECURITY_DOWNGRADE("3150",20,"关闭双重认证"),
    SUPPORT_PIN("3160",30,"生成支持PIN"),
    APPLE_ID_OVERCHECK("3170",100,"AppleID过检"),
    /********** QUERY区 ***********/
    RAPID_FILTRATION("4110",5,"极速过滤密正"),
    DETECTION_WHETHER("4120",5,"检测是否过检"),
    WHETHER_APPLEID("4130",10,"检测是否AppleID"),
    BIRTHDAY_COUNTRY_QUERY("4140",20,"查询生日国家"),
    SECURITY_QUESTION("4150",5,"查询密保问题"),
    CHECK_GRAY_BALANCE("4160",20,"检测灰余额"),
    BALANCE_QUERY("4170",20,"密保查询余额"),

    /********** TOOLBOX区 ***********/

    ;
    private String     code;
    private Integer    point;
    private String     desc;

    FunctionListEnum(String code, Integer point, String desc) {
        this.code = code;
        this.point = point;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public static FunctionListEnum getFunEnumByCode(String code) {
        for (FunctionListEnum myEnum : values()) {
            if(code.equals(myEnum.code)){
                return myEnum;
            }
        }
        return null;
    }

    public static FunctionListEnum getFunEnumByDesc(String desc) {
        for (FunctionListEnum myEnum : values()) {
            if(desc.equals(myEnum.desc)){
                return myEnum;
            }
        }
        return null;
    }
}

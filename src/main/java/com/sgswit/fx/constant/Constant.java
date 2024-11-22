package com.sgswit.fx.constant;

import java.util.HashMap;
import java.util.Map;

public class Constant {
    public static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36";

    public static final String BROWSER_CLIENT_INFO = "{\"U\":\"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"kla44j1e3NlY5BNlY5BSmHACVZXnNA9dEQg0F..B1zLu_dYV6Hycfx9MsFY5C7JajV2pNk0ug9WJ3u6eRMfuZjn4Ukd5BNlY5CGWY5BOgkLT0XxU..B6g\"}";



    public static final String CONFIGURATOR_USER_AGENT = "Configurator/2.15 (Macintosh; OS X 11.0.0; 16G29) AppleWebKit/2603.3.8";
    public static final String MACAPPSTORE20_USER_AGENT ="MacAppStore/2.0 (Macintosh; OS X 12.10) AppleWebKit/600.1.3.41";
    public static final String XMMeClientInfo ="<MacBook Pro> <Mac OS X;10.10;14A314h> <com.apple.AOSKit/203 (com.apple.systempreferences/14.0)>";
    public static final String ICLOUND_USER_AGENT ="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10) AppleWebKit/600.1.3 (KHTML, like Gecko)";



    public static final String 	SUCCESS     = "200";
    public static final String 	REDIRECT_CODE     = "302";
    public static final String 	TWO_FACTOR_AUTHENTICATION     = "401";
    public static final String 	FailureTypeInvalidCredentials     = "-5000";

    public static final String CustomerMessageNotYetUsediTunesStore = "has not yet been used with the iTunes Store.";
    public static final String CustomerMessageNotYetUsediTunesStoreCode = "5001";
    public static final String ACCOUNT_IS_DISABLED = "Your account is disabled.";
    public static final String  ACCOUNT_HAS_BEEN_LOCKED = "You cannot login because your account has been locked";

    public static final String AppleIdOrPasswordIncorrectly = "Your Apple ID or password was entered incorrectly.";

    public static final String CustomerMessageBadLogin             = "MZFinance.BadLogin.Configurator_message";
    public static final String MZFinanceDisabledAndFraudLocked = "MZFinance.DisabledAndFraudLocked";
    public static final String MZFinanceAccountDisabled = "MZFinance.AccountDisabled";
    public static final String MZFinanceAccountConversion = "MZFinance.AccountConversion";

    public static final String HTTPHeaderStoreFront = "X-Set-Apple-Store-Front";

    public static final String ITSPOD = "itspod";

    public static final String ACCOUNT_INVALID_HSA_TOKEN = "ACCOUNT_INVALID_HSA_TOKEN";

    public static final String LOCAL_FILE_STORAGE_PATH = System.getProperty("user.dir") + "/fileSystem";

    public static final String EXCEL_EXPORT_PATH = System.getProperty("user.dir") + "/导出";

    public static final String LOCAL_FILE_EXTENSION = ".applebatch";

    public static final String REDEEM_WAIT1_DESC = "兑换暂不可用，将在一分钟之后执行";
    public static final String REDEEM_WAIT2_DESC = "此账号兑换太过频繁，请等待一分钟后再试。";

    public static final String MAC_DB_URL = System.getProperty("user.home") + "/.db/xg.sqlite";

    public static final String WIN_DB_URL = "xg.sqlite";


    public static final Map<String,String> errorMap = new HashMap<>(){{
        put(ACCOUNT_IS_DISABLED,"出于安全原因，你的账户已被锁定。");
        put(ACCOUNT_HAS_BEEN_LOCKED,"帐户存在欺诈行为，已被【双禁】。");
        put(CustomerMessageBadLogin,"Apple ID或密码错误。或需要输入验证码！");
        put(CustomerMessageNotYetUsediTunesStore,"此 Apple ID 尚未用于 App Store。");
        put(AppleIdOrPasswordIncorrectly,"Apple ID或密码错误。");
    }};

    /**
    　* 右键菜单枚举类
    　* @author DeZh
    　* @date 2023/12/23 18:55
    */
    public enum RightContextMenu{
        DELETE("delete","删除",""),
        REEXECUTE("reexecute","重新执行",""),
        COPY("copy","复制账号信息",""),
        COPY_ALL("copyAll","复制全部信息",""),
        COPY_CARD_NO("copyCardNo","复制礼品卡代码",""),
        TWO_FACTOR_CODE("twoFactorCode","输入双重验证码","views/comm-code-popup.fxml"),
        WEB_TWO_FACTOR_CODE("webTwoFactorCode","输入双重验证码","views/securitycode-popup.fxml"),
        CODE("code","输入验证码","views/comm-code-popup.fxml"),
        ;
        private String code;
        private String title;
        private String path;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        RightContextMenu(String code,String title, String path) {
            this.code = code;
            this.title = title;
            this.path = path;
        }
    }

}

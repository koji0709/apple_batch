package com.sgswit.fx.constant;

import java.util.HashMap;
import java.util.Map;

public class Constant {

    public static final String 	FailureTypeInvalidCredentials     = "-5000";
    public static final String  FailureTypePasswordTokenExpired   = "2034";
    public static final String FailureTypeLicenseNotFound        = "9610";
    public static final String FailureTypeTemporarilyUnavailable = "2059";

    public static final String CustomerMessageNotYetUsediTunesStore = "has not yet been used with the iTunes Store.";

    public static final String CustomerMessageBadLogin             = "MZFinance.BadLogin.Configurator_message";
    public static final String CustomerMessageSubscriptionRequired = "Subscription Required";

    public static final String HTTPHeaderStoreFront = "X-Set-Apple-Store-Front";
    public static final String HTTPCookies = "Set-Cookie";

    public static final String ITSPOD = "itspod";

    public static final String X_APPLE_ORIG_URL = "x-apple-orig-url";
    public static final String ACCOUNT_INVALID_HSA_TOKEN = "ACCOUNT_INVALID_HSA_TOKEN";
    public static final String LOCAL_FILE_STORAGE_PATH = System.getProperty("user.home") + "/APPLE-BATCH";

    public static final String LOCAL_FILE_EXTENSION = ".applebatch";

    public static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36";

    public static final Map<String,String> errorMap = new HashMap<>(){{
        put("your account is disabled.","出于安全原因，你的账户已被锁定。");
        put("You cannot login because your account has been locked","帐户存在欺诈行为，已被【双禁】。");
        put(CustomerMessageBadLogin,"Apple ID或密码错误。或需要输入验证码！");
        put(CustomerMessageNotYetUsediTunesStore,"此 Apple ID 尚未用于 App Store。");
    }};
}

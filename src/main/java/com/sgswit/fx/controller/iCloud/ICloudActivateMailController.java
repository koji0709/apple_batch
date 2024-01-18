package com.sgswit.fx.controller.iCloud;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ICloudView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.ICloudUtil;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;


public class ICloudActivateMailController extends ICloudView<Account> {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        menuItem.add(Constant.RightContextMenu.WEB_TWO_FACTOR_CODE.getCode());
    }

    /**
     * 导入账号
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----pwd", "account----pwd----email"));
    }

    @Override
    public void accountHandler(Account account) {
        iCloudLogin(account);
        if (!account.isLogin()) {
            return;
        }

        Map<String, String> signInMap = (Map<String, String>) account.getAuthData().get("signInMap");
        HttpResponse accountLoginRsp = (HttpResponse) account.getAuthData().get("accountLoginRsp");
        JSONObject jo = JSONUtil.parseObj(accountLoginRsp.body());

        String iCloudAppleIdAlias = jo.getByPath("dsInfo.iCloudAppleIdAlias").toString();
        if (!StrUtil.isEmpty(iCloudAppleIdAlias)) {
            throw new ServiceException("该账号已存在专属邮箱:" + iCloudAppleIdAlias);
        }

        account.updateLoginInfo(accountLoginRsp);
        String domain = signInMap.get("domain");
        JSONObject webservices = (JSONObject) jo.getByPath("webservices");
        if (!webservices.containsKey("mccgateway")) {
            throw new ServiceException("该账号不能激活邮箱");
        }

        // https://p218-mccgateway.icloud.com.cn:443
        String url = webservices.getJSONObject("mccgateway").getStr("url");
        // 去除:443
        url = url.substring(0, url.length() - 3);
        // 获取218
        String pNum = url.substring(9, url.indexOf("-"));

        // 如果用户设置了,就使用用户设置的邮箱
        String email = account.getEmail();

        // 获取推荐邮箱
        if (StrUtil.isEmpty(email)) {
            HttpResponse emailSuggestionsRsp = ICloudUtil.emailSuggestions(account, pNum, domain);
            if (emailSuggestionsRsp.getStatus() == 200) {
                List<String> suggestions = JSONUtil.parseObj(emailSuggestionsRsp.body()).getByPath("suggestions.name", List.class);
                if (!CollUtil.isEmpty(suggestions)) {
                    email = suggestions.get(0);
                }
            }
        }

        // 如果没有配置好邮箱,则随机生成(必须最多20个字符,且是字母开头)
        if (StrUtil.isEmpty(email)) {
            String prefix = account.getAccount();
            if (NumberUtil.isNumber(prefix)) {
                prefix = "m" + prefix;
            } else {
                int i = prefix.indexOf("@");
                if (i != -1) {
                    prefix = prefix.substring(0, i);
                }
            }
            prefix = prefix + RandomUtil.randomNumbers(prefix.length() < 4 ? 8 : 4);
            if (prefix.length() > 20){
                prefix = prefix.substring(0,20);
            }
            email = prefix + "@icloud.com";
        }

        // 检测邮箱是否可用
        Boolean available = false;
        HttpResponse emailAvailabilityRsp = ICloudUtil.emailAvailability(account, pNum, domain, email);
        if (emailAvailabilityRsp.getStatus() == 200) {
            JSONObject emailAvailabilityBody = JSONUtil.parseObj(emailAvailabilityRsp.body());
            available = emailAvailabilityBody.getBool("available", false);
        }

        if (!available) {
            throw new ServiceException("该邮箱不可用");
        }

        HttpResponse activateEmailRsp = ICloudUtil.activateEmail(account, pNum, domain, email);
        if (activateEmailRsp.getStatus() != 200) {
            throw new ServiceException("邮箱激活失败");
        }

        account.setEmail(email);
        setAndRefreshNote(account, "邮箱激活成功");
    }

    @Override
    protected void reExecute(Account account) {
        account.setSecurityCode("");
        accountHandlerExpand(account);
    }
}

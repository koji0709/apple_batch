package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ItunesView;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.*;

import java.util.List;


public class CheckBalanceDisabledController extends ItunesView<Account> {

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----pwd"));
    }

    /**
     * 账号处理
     * djli0506@163.com----!!B0527s0207!!
     */
    @Override
    public void accountHandler(Account account) {
        String guid = DataUtil.getGuidByAppleId(account.getAccount());
        account.setGuid(guid);
        itunesLogin(account);

        HttpResponse authRsp = (HttpResponse)account.getAuthData().get("authRsp");
        JSONObject rspJSON = PListUtil.parse(authRsp.body());
        String balance  = rspJSON.getStr("creditDisplay","0");
        Boolean isDisabledAccount  = rspJSON.getBool("accountFlags.isDisabledAccount",false);
        account.setBalance((StrUtil.isEmpty(balance) ? "0" : balance));
        account.setDisableStatus( !isDisabledAccount ? "正常" : "禁用");

        String storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
        String country = StoreFontsUtils.getCountryCode(StrUtil.split(storeFront, "-").get(0));
        if (!StrUtil.isEmpty(country)){
            String[] sp = country.split("-");
            account.setAreaCode(sp[0]);
            account.setArea(sp[1]);
        }
        setAndRefreshNote(account,"查询成功");
    }

    @Override
    protected void secondStepHandler(Account account, String code) {
        account.setAuthCode(code);
        accountHandler(account);
    }

    @Override
    protected void reExecute(Account o) {
        accountHandler(o);
    }

}

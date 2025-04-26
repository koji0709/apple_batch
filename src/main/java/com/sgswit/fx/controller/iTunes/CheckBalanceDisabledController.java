package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.constant.StoreFontsUtils;
import com.sgswit.fx.controller.common.ItunesView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.PListUtil;
import javafx.event.ActionEvent;

import java.util.List;


public class CheckBalanceDisabledController extends ItunesView<Account> {

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction(ActionEvent actionEvent) {
        openImportAccountView(List.of("account----pwd"),actionEvent);
    }

    @Override
    public Long getIntervalFrequency() {
        return 500L;
    }

    /**
     * 账号处理
     */
    @Override
    public void accountHandler(Account account) {
        setAndRefreshNote(account,"登录查询中...");
        String id=super.createId(account.getAccount(),account.getPwd());
        loginSuccessMap.remove(id);
        itunesLogin(account);
        HttpResponse authRsp = (HttpResponse)account.getAuthData().get("authRsp");
        JSONObject rspJSON = PListUtil.parse(authRsp.body());
        String balance  = rspJSON.getStr("creditDisplay","0");
        Boolean isDisabledAccount  = rspJSON.getByPath("accountFlags.isDisabledAccount",Boolean.class);
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
        accountHandlerExpand(account);
    }

}

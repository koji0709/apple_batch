package com.sgswit.fx.controller.operation;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.common.AppleIdView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;

import java.util.Date;
import java.util.List;


/**
 * 生成支持PIN controller
 */
public class SupportPinController extends AppleIdView {

    /**
     * 导入账号
     */
    public void importAccountButtonAction(){
        openImportAccountView(List.of("account----pwd-answer1-answer2-answer3"));
    }

    @Override
    public void accountHandler(Account account) {
        // 登陆
        String scnt = loginAndGetScnt(account);
        if (StrUtil.isEmpty(scnt)){
            return;
        }

        HttpResponse supportPinRsp = AppleIDUtil.supportPin(scnt);

        if (supportPinRsp.getStatus() == 503){
            setAndRefreshNote(account,"操作频繁");
            return;
        }

        String body = supportPinRsp.body();
        if (StrUtil.isEmpty(body) || !JSONUtil.isTypeJSON(body)){
            setAndRefreshNote(account,"生成支持PIN失败！");
            return;
        }

        String errorMessage = hasFailMessage(supportPinRsp) ? failMessage(supportPinRsp) : "生成支持PIN失败";

        JSON parse = JSONUtil.parse(body);
        String pin = parse.getByPath("pin", String.class);
        if (StrUtil.isEmpty(pin)){
            setAndRefreshNote(account, errorMessage);
            return;
        }

        account.setPin(pin);
        account.setPinExpir(DateUtil.format(DateUtil.offsetMinute(new Date(), 30),"yyyy-MM-dd HH:mm"));
        setAndRefreshNote(account,"生成支持PIN成功!");
    }
}

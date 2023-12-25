package com.sgswit.fx.controller.operation;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.AppleIdView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.scene.input.ContextMenuEvent;

import java.util.ArrayList;
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
        openImportAccountView(List.of("account----pwd","account----pwd-answer1-answer2-answer3"));
    }

    @Override
    public void accountHandler(Account account) {
        // 登陆
        login(account);

        HttpResponse supportPinRsp = AppleIDUtil.supportPin(account);

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

        JSON bodyJSON = JSONUtil.parse(body);
        String pin = bodyJSON.getByPath("pin", String.class);
        //String pinExpir = bodyJSON.getByPath("localizedDate", String.class);
        if (StrUtil.isEmpty(pin)){
            setAndRefreshNote(account, errorMessage);
            return;
        }

        account.setPin(pin);
        account.setPinExpir(DateUtil.format(DateUtil.offsetMinute(new Date(), 30),"yyyy-MM-dd HH:mm"));
        setAndRefreshNote(account,"生成支持PIN成功!");
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> menuItem =new ArrayList<>(){{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
            add(Constant.RightContextMenu.WEB_TWO_FACTOR_CODE.getCode());
        }};
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItem,new ArrayList<>());
    }

    @Override
    protected void reExecute(Account account) {
        account.setNote("");
        accountHandler(account);
    }

}

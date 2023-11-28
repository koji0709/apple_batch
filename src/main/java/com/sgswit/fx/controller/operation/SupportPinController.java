package com.sgswit.fx.controller.operation;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.base.AppleIdView;
import com.sgswit.fx.controller.base.TableView;
import com.sgswit.fx.controller.operation.viewData.SecurityUpgradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


/**
 * 生成支持PIN controller
 */
public class SupportPinController extends AppleIdView {

    /**
     * 导入账号
     */
    public void importAccountButtonAction(){
        openImportAccountView("account----pwd-answer1-answer2-answer3");
    }

    @Override
    public void accountHandler(Account account) {
        // 登陆
        String scnt = loginAndGetScnt(account);
        if (StrUtil.isEmpty(scnt)){
            return;
        }

        HttpResponse supportPinRsp = AppleIDUtil.supportPin(scnt);
        setAndRefreshNote(account,"生成支持PIN失败！");

        String body = supportPinRsp.body();
        if (!StrUtil.isEmpty(body) && JSONUtil.isTypeJSON(body)){
            JSON parse = JSONUtil.parse(body);
            String pin = parse.getByPath("pin", String.class);
            if (!StrUtil.isEmpty(pin)){
                account.setPin(pin);
                account.setPinExpir(DateUtil.format(DateUtil.offsetMinute(new Date(), 30),"yyyy-MM-dd HH:mm"));
                setAndRefreshNote(account,"生成支持PIN成功!");
            }
        }
    }
}

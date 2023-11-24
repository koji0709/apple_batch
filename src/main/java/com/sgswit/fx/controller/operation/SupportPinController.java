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
import java.util.Date;
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

    /**
     * 开始执行按钮点击
     */
    public void executeButtonAction(){
        // 校验
        if (accountList.isEmpty()){
            alert("请先导入账号！");
            return;
        }

        for (Account account : accountList) {
            // 检测账号是否被处理过
            boolean processed = isProcessed(account);
            if (processed){
                continue;
            }

            setAndRefreshNote(account,"执行中");

            // 登陆
            String scnt = loginAndGetScnt(account);
            if (StrUtil.isEmpty(scnt)){
                continue;
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

}

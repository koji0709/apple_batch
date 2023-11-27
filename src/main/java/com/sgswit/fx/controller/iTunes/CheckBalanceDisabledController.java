package com.sgswit.fx.controller.iTunes;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import com.sgswit.fx.controller.base.TableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.PropertiesUtil;

import java.util.ArrayList;
import java.util.List;

public class CheckBalanceDisabledController extends TableView<Account> {

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView("account----pwd");
    }

    /**
     * 开始执行按钮点击
     * djli0506@163.com----!!B0527s0207!!
     */
    public void executeButtonAction(){
        // 校验
        if (accountList.isEmpty()){
            alert("请先导入账号！");
            return;
        }

        String guid = PropertiesUtil.getOtherConfig("guid");

        List<Account> recordList = new ArrayList<>();
        for (Account account : accountList) {
            boolean processed = isProcessed(account);
            if (processed){
                continue;
            }

            setAndRefreshNote(account,"执行中");
            HttpResponse authenticateRsp = ITunesUtil.authenticate(account, guid);
            if (authenticateRsp.getStatus() == 200){
                NSObject rspNO = null;
                try {
                    rspNO = XMLPropertyListParser.parse(authenticateRsp.body().getBytes("UTF-8"));
                } catch (Exception e) {
                    setAndRefreshNote(account,"查询失败");
                    continue;
                }

                JSONObject rspJSON = (JSONObject) JSONUtil.parse(rspNO.toJavaObject());
                String balance  = rspJSON.getByPath("creditDisplay",String.class);
                Boolean isDisabledAccount  = rspJSON.getByPath("accountFlags.isDisabledAccount",Boolean.class);
                account.setBalance(balance);
                account.setDisableStatus( !isDisabledAccount ? "正常" : "禁用");

                // todo 查询区域和区域代码
                account.setArea("");
                account.setAreaCode("");

                setAndRefreshNote(account,"查询成功");
            } else if (authenticateRsp.getStatus() == 503){
                setAndRefreshNote(account,"操作频繁。");
            }else{
                setAndRefreshNote(account,"AppleID或密码错误，或需输入双重验证码。");
            }
            recordList.add(account);
        }
        insertLocalHistory(recordList);
    }

}

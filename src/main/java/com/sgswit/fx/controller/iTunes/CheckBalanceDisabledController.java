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

public class CheckBalanceDisabledController extends TableView {

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

        for (Account account : accountList) {
            HttpResponse authenticateRsp = ITunesUtil.authenticate(account, guid);
            if (authenticateRsp.getStatus() == 200){
                NSObject rspNO = null;
                try {
                    rspNO = XMLPropertyListParser.parse(authenticateRsp.body().getBytes("UTF-8"));
                } catch (Exception e) {
                    account.setNote("查询失败");
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
            } else {
                account.setNote("AppleID或密码错误，或需输入双重验证码。");
            }

        }

    }
}

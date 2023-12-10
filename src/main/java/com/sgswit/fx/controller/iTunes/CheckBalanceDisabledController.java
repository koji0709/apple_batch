package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.PropertiesUtil;


public class CheckBalanceDisabledController extends TableView<Account> {

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView("account----pwd");
    }

    /**
     * 账号处理
     * djli0506@163.com----!!B0527s0207!!
     */
    @Override
    public void accountHandler(Account account) {
        String guid = DataUtil.getGuidByAppleId(account.getAccount());
        HttpResponse authenticateRsp = ITunesUtil.authenticate(account, guid);
        if (authenticateRsp.getStatus() == 200){
            NSObject rspNO = null;
            try {
                rspNO = XMLPropertyListParser.parse(authenticateRsp.body().getBytes("UTF-8"));
            } catch (Exception e) {
                setAndRefreshNote(account,"查询失败");
                return;
            }

            JSONObject rspJSON = (JSONObject) JSONUtil.parse(rspNO.toJavaObject());
            String failureType = rspJSON.getStr("failureType");
            String customerMessage = rspJSON.getStr("customerMessage");
            if (!StrUtil.isEmpty(failureType) || !StrUtil.isEmpty(customerMessage)){
                if (!StrUtil.isEmpty(customerMessage)){
                    setAndRefreshNote(account,customerMessage);
                    return;
                }
                if (!StrUtil.isEmpty(failureType)){
                    setAndRefreshNote(account,failureType);
                    return;
                }
            }
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
    }
}

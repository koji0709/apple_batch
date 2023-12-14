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
import com.sgswit.fx.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
            String balance  = rspJSON.getStr("creditDisplay","0");
            Boolean isDisabledAccount  = rspJSON.getBool("accountFlags.isDisabledAccount",false);
            account.setBalance(balance);
            account.setDisableStatus( !isDisabledAccount ? "正常" : "禁用");
            String message=rspJSON.getByPath("dialog.message",String.class);
            String pattern = "(?i)此 Apple ID 只能在(.*)购物";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(message);
            String areaName="";
            while (m.find()) {
                System.out.println(m.group(1));
                areaName=m.group(1);
            }
            String areaCode= DataUtil.getCodeByCountryName(areaName);
            account.setArea(areaName);
            account.setAreaCode(areaCode);

            setAndRefreshNote(account,"查询成功");
        } else if (authenticateRsp.getStatus() == 503){
            setAndRefreshNote(account,"操作频繁。");
        }else{
            setAndRefreshNote(account,"AppleID或密码错误，或需输入双重验证码。");
        }
    }
}

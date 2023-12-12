package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.PListUtil;

public class GiftCardBatchRedeemController extends TableView<GiftCardRedeem> {

    public void importAccountButtonAction() {
        openImportAccountView(GiftCardRedeem.class,"account----pwd----giftCardCode");
    }

    /**
     * qewqeq@2980.com----dPFb6cSD41----XMPC3HRMNM6K5FXP
     * @param giftCardRedeem
     */
    @Override
    public void accountHandler(GiftCardRedeem giftCardRedeem) {

        giftCardRedeem.setExecTime(DateUtil.now());

        Account account = new Account();
        account.setAccount(giftCardRedeem.getAccount());
        account.setPwd(giftCardRedeem.getPwd());

        String guid = DataUtil.getGuidByAppleId(account.getAccount());
        HttpResponse authRsp = ITunesUtil.authenticate(account,guid);

        // 鉴权
        boolean verify = itunesLoginVerify(authRsp,giftCardRedeem);
        if (!verify){
            return;
        }

        String giftCardCode = giftCardRedeem.getGiftCardCode();
        HttpResponse redeemRsp = ITunesUtil.redeem(authRsp, guid, giftCardCode);
        if (redeemRsp.getStatus() != 200){
            String message = "礼品卡[%s]兑换失败! %s";
            Console.log(String.format(message,giftCardCode));
            setAndRefreshNote(giftCardRedeem,"兑换失败!");
            return;
        }

        // 兑换
        String body = redeemRsp.body();
        JSONObject redeemBody = JSONUtil.parseObj(body);
        Integer status = redeemBody.getInt("status",-1);
        if (status != 0){
            String userPresentableErrorMessage = redeemBody.getStr("userPresentableErrorMessage");
            String message = "礼品卡[%s]兑换失败! %s";
            message = String.format(message,giftCardCode,userPresentableErrorMessage);
            Console.log(message);
            setAndRefreshNote(giftCardRedeem,message);
            return;
        }
        String message = "礼品卡[%s]兑换成功!";
        message = String.format(message,giftCardCode);
        setAndRefreshNote(giftCardRedeem,message);
    }

}

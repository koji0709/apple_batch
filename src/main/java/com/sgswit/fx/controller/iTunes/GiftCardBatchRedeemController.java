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
import com.sgswit.fx.controller.common.ItunesView;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.*;

public class GiftCardBatchRedeemController extends ItunesView<GiftCardRedeem> {

    @FXML
    ComboBox<String> accountComboBox;

    @FXML
    Label countryLabel;

    @FXML
    Label blanceLabel;

    @FXML
    Label statusLabel;

    @FXML
    Label checkAccountDescLabel;

    /**
     * 导入账号
     */
    public void importAccountButtonAction() {
        String desc = "说明：\n" +
                "    1.格式为: 账号----密码----礼品卡(可多个) 或 单礼品卡\n" +
                "    2.一次可以输入多条账户信息，每条账户单独一行; 如果数据中有“-”符号,则使用{-}替换。";
        openImportAccountView(Collections.emptyList(),desc);
    }

    @Override
    public List<GiftCardRedeem> parseAccount(String accountStr){
        List<GiftCardRedeem> accountList1 = new ArrayList<>();
        accountStr = accountStr.replaceAll("----","-");
        if (accountStr.contains("{-}")){
            accountStr = accountStr.replace("{-}",AccountImportUtil.REPLACE_MENT);
        }

        String[] accList = accountStr.split("\n");

        for (int i = 0; i < accList.length; i++) {
            String acc = accList[i];
            if (StrUtil.isEmpty(acc)){
                continue;
            }
            List<String> valList = Arrays.asList(acc.split("-"));
            // 单礼品卡模式
            if (valList.size()==1){
                String accountComboBoxValue = accountComboBox.getValue();
                if (StrUtil.isEmpty(accountComboBoxValue)){
                    continue;
                }
                String[] accountComboBoxValueArr = accountComboBoxValue.split("----");
                if (accountComboBoxValueArr.length != 2){
                    continue;
                }
                String account = accountComboBoxValueArr[0];
                String pwd     = accountComboBoxValueArr[1];
                GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
                giftCardRedeem.setAccount(account);
                giftCardRedeem.setPwd(pwd);
                giftCardRedeem.setGiftCardCode(valList.get(0));
                accountList1.add(giftCardRedeem);
            }else{// 账号礼品卡模式
                if (valList.size() >= 3){
                    String account = valList.get(0);
                    String pwd     = valList.get(1);
                    for (int j = 2; j < valList.size(); j++) {
                        GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
                        giftCardRedeem.setAccount(account);
                        giftCardRedeem.setPwd(pwd);
                        giftCardRedeem.setGiftCardCode(valList.get(j));
                        accountList1.add(giftCardRedeem);
                    }
                }
            }
        }

        return accountList1;
    }

    /**
     * qewqeq@2980.com----dPFb6cSD41----XMPC3HRMNM6K5FXP
     */
    @Override
    public void accountHandler(GiftCardRedeem giftCardRedeem) {

        giftCardRedeem.setExecTime(DateUtil.now());

        Account account = new Account();
        account.setAccount(giftCardRedeem.getAccount());
        account.setPwd(giftCardRedeem.getPwd());

        String guid = DataUtil.getGuidByAppleId(account.getAccount());
        HttpResponse authRsp = itunesLogin(giftCardRedeem, guid, true);

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
            String messageKey = redeemBody.getStr("errorMessageKey","");

            // 礼品卡无效
            if ("MZFreeProductCode.NoSuch".equals(messageKey)){
                giftCardRedeem.setGiftCardStatus("无效");
            } else if ("MZCommerce.GiftCertificateAlreadyRedeemed".equals(messageKey)){// 礼品卡已兑换
                giftCardRedeem.setGiftCardStatus("已兑换");
            }else{
                giftCardRedeem.setGiftCardStatus("兑换失败");
            }

            String message = "礼品卡[%s]兑换失败! %s";
            message = String.format(message,giftCardCode,userPresentableErrorMessage);
            Console.log(message);
            setAndRefreshNote(giftCardRedeem,message);
            return;
        }
        // 礼品卡兑换成功
        String message = "礼品卡[%s]兑换成功!";
        giftCardRedeem.setGiftCardStatus("兑换成功");

        message = String.format(message,giftCardCode);
        setAndRefreshNote(giftCardRedeem,message);
    }

    /**
     * 检测账号按钮点击
     */
    public void checkAccountBtnAction(){
        String accountComboBoxValue = accountComboBox.getValue();
        if (StrUtil.isEmpty(accountComboBoxValue)){
            checkAccountDescLabel.setText("请先输入账号信息");
            return;
        }
        String[] accountComboBoxValueArr = accountComboBoxValue.split("----");
        if (accountComboBoxValueArr.length != 2){
            checkAccountDescLabel.setText("账号信息格式不正确！格式：账号----密码");
            return;
        }

        checkAccountDescLabel.setText("");
        whatsName("查询中...");

        String account = accountComboBoxValueArr[0];
        String pwd     = accountComboBoxValueArr[1];
        String guid = DataUtil.getGuidByAppleId(account);

        GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
        giftCardRedeem.setAccount(account);
        giftCardRedeem.setPwd(pwd);

        HttpResponse authRsp = itunesLogin(giftCardRedeem, guid, true);
        boolean verify = itunesLoginVerify(authRsp, giftCardRedeem);
        if (!verify){
            whatsName("");
            checkAccountDescLabel.setText(giftCardRedeem.getNote());
            return;
        }

        String storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
        String country = StoreFontsUtils.getCountryCode(StrUtil.split(storeFront, "-").get(0));
        country = StrUtil.isEmpty(country) ? "未知" : country.split("-")[1];
        JSONObject rspJSON = PListUtil.parse(authRsp.body());
        String  balance           = rspJSON.getStr("creditDisplay","0");
        Boolean isDisabledAccount = rspJSON.getBool("accountFlags.isDisabledAccount",false);
        String  status            = !isDisabledAccount ? "正常" : "禁用";

        countryLabel.setText("国家：" + country);
        blanceLabel.setText( "余额：" + (StrUtil.isEmpty(balance) ? "0" : balance));
        statusLabel.setText( "状态：" + status);
    }

    public void whatsName(String message){
        countryLabel.setText("国家：" + message);
        blanceLabel.setText( "余额：" + message);
        statusLabel.setText( "状态：" + message);
    }

    /**
     * 礼品卡查询余额按钮点击
     */
    public void giftCardBlanceBtnAction(){
        StageUtil.show(StageEnum.GIFTCARD_BLANCE);
    }

}

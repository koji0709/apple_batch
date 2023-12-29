package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CommCodePopupView;
import com.sgswit.fx.controller.common.ItunesView;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @FXML
    Button checkAccountBtn;

    @FXML
    Button open2FAViewBtn;

    @FXML
    Button editOrImportAccountListBtn;

    private GiftCardRedeem singleGiftCardRedeem = new GiftCardRedeem();

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
                String giftCardCode = valList.get(0);
                String account = accountComboBoxValueArr[0];
                String pwd     = accountComboBoxValueArr[1];
                GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
                giftCardRedeem.setAccount(account);
                giftCardRedeem.setPwd(pwd);
                giftCardRedeem.setGiftCardCode(giftCardCode);

                boolean success = giftCardCodeVerify(giftCardCode);
                if (!success){
                    giftCardRedeem.setGiftCardStatus("无效");
                }

                accountList1.add(giftCardRedeem);
            }else{// 账号礼品卡模式
                if (valList.size() >= 3){
                    String account = valList.get(0);
                    String pwd     = valList.get(1);
                    for (int j = 2; j < valList.size(); j++) {
                        String giftCardCode = valList.get(j);
                        GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
                        giftCardRedeem.setAccount(account);
                        giftCardRedeem.setPwd(pwd);
                        giftCardRedeem.setGiftCardCode(giftCardCode);

                        boolean success = giftCardCodeVerify(giftCardCode);
                        if (!success){
                            giftCardRedeem.setGiftCardStatus("无效");
                        }

                        accountList1.add(giftCardRedeem);
                    }
                }
            }
        }

        return accountList1;
    }

    /**
     * qewqeq@2980.com----dPFb6cSD41----XMPC3HRMNM6K5FXP
     * cncots@gmail.com----Xx97595031.----XMPC3HRMNM6K5FXP
     */
    @Override
    public void accountHandler(GiftCardRedeem giftCardRedeem) {
        giftCardRedeem.setExecTime(DateUtil.now());
        boolean success = giftCardCodeVerify(giftCardRedeem.getGiftCardCode());
        if (!success){
            setAndRefreshNote(giftCardRedeem,"输入的代码无效。");
            return;
        }

        String guid = DataUtil.getGuidByAppleId(giftCardRedeem.getAccount());
        giftCardRedeem.setGuid(guid);
        itunesLogin(giftCardRedeem);

        String giftCardCode = giftCardRedeem.getGiftCardCode();
        HttpResponse redeemRsp = ITunesUtil.redeem(giftCardRedeem);
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

    @Override
    protected void secondStepHandler(GiftCardRedeem account, String code) {
        account.setAuthCode(code);
        accountHandler(account);
    }

    @Override
    protected void reExecute(GiftCardRedeem o) {
        accountHandler(o);
    }


    /**
     * 检测账号按钮点击
     */
    public void checkAccountBtnAction(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String accountComboBoxValue = accountComboBox.getValue();
                    if (StrUtil.isEmpty(accountComboBoxValue)){
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                checkAccountDescLabel.setText("请先输入账号信息");
                            }
                        });

                        return;
                    }
                    String[] accountComboBoxValueArr = accountComboBoxValue.split("----");
                    if (accountComboBoxValueArr.length != 2){
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                checkAccountDescLabel.setText("账号信息格式不正确！格式：账号----密码");
                            }
                        });
                        return;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            checkAccountDescLabel.setText("");
                            statusLabel.setText( "状态：" + "正在检测...");
                            checkAccountBtn.setDisable(true);
                            open2FAViewBtn.setDisable(true);
                            editOrImportAccountListBtn.setDisable(true);
                        }
                    });
                    String account = accountComboBoxValueArr[0];
                    String pwd     = accountComboBoxValueArr[1];
                    String guid = DataUtil.getGuidByAppleId(account);
                    if (StrUtil.isEmpty(singleGiftCardRedeem.getAccount())
                            || !account.equals(singleGiftCardRedeem.getAccount())
                            || !pwd.equals(singleGiftCardRedeem.getAccount())
                            || !singleGiftCardRedeem.isLogin()){
                        singleGiftCardRedeem.setAccount(account);
                        singleGiftCardRedeem.setPwd(pwd);
                        singleGiftCardRedeem.setGuid(guid);
                        singleGiftCardRedeem.setNote("");
                        singleGiftCardRedeem.setIsLogin(false);
                        itunesLogin(singleGiftCardRedeem);
                    }

                    HttpResponse authRsp = (HttpResponse) singleGiftCardRedeem.getAuthData().get("authRsp");
                    String storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
                    String country = StoreFontsUtils.getCountryCode(StrUtil.split(storeFront, "-").get(0));
                    country = StrUtil.isEmpty(country) ? "未知" : country.split("-")[1];
                    JSONObject rspJSON = PListUtil.parse(authRsp.body());
                    String  balance           = rspJSON.getStr("creditDisplay","0");
                    Boolean isDisabledAccount = rspJSON.getBool("accountFlags.isDisabledAccount",false);
                    String  status            = !isDisabledAccount ? "正常" : "禁用";
                    String finalCountry = country;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            countryLabel.setText("国家：" + finalCountry);
                            blanceLabel.setText( "余额：" + (StrUtil.isEmpty(balance) ? "0" : balance));
                            statusLabel.setText( "状态：" + status);
                        }
                    });
                }catch (ServiceException e){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            checkAccountDescLabel.setText(singleGiftCardRedeem.getNote());
                            countryLabel.setText("国家：" + "");
                            blanceLabel.setText( "余额：" + "");
                            statusLabel.setText( "状态：" + singleGiftCardRedeem.getNote());
                        }
                    });
                }catch (Exception e){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            checkAccountDescLabel.setText("接口数据处理异常");
                            countryLabel.setText("国家：" + "");
                            blanceLabel.setText( "余额：" + "");
                            statusLabel.setText( "状态：" + "接口数据处理异常");
                        }
                    });
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            checkAccountBtn.setDisable(false);
                            open2FAViewBtn.setDisable(false);
                            editOrImportAccountListBtn.setDisable(false);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 输入双重验证码
     */
    public void open2FAViewBtnAction(){
        if (StrUtil.isEmpty(singleGiftCardRedeem.getAccount())){
            alert("请先检测账号");
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/comm-code-popup.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 385, 170);
        } catch (IOException e) {
            //throw new RuntimeException(e);
            alert(e.getMessage());
            return;
        }

        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        CommCodePopupView fxmlLoaderController = fxmlLoader.getController();
        fxmlLoaderController.setAccount(singleGiftCardRedeem.getAccount());
        Stage popupStage = new Stage();
        popupStage.setTitle("输入验证码");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();
        String code = fxmlLoaderController.getSecurityCode();
        if(StringUtils.isEmpty(code)){
            return;
        }
        singleGiftCardRedeem.setAuthCode(code);
        checkAccountBtnAction();
    }

    /**
     * 礼品卡查询余额按钮点击
     */
    public void giftCardBlanceBtnAction(){
        StageUtil.show(StageEnum.GIFTCARD_BLANCE);
    }

    /**
     * 编辑或导入列表中的账号
     */
    public void editOrImportAccountListBtnAction(){
        alert("功能开发中");
    }

    /**
     * 礼品卡校验
     */
    public boolean giftCardCodeVerify(String giftCardCode){
        //判断礼品卡的格式是否正确
        String regex = "X[a-zA-Z0-9]{15}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(giftCardCode.toUpperCase());
        return matcher.matches();
    }

}

package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.constant.StoreFontsUtils;
import com.sgswit.fx.controller.common.CommCodePopupView;
import com.sgswit.fx.controller.common.ItunesView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiftCardBatchPEController extends ItunesView<GiftCardRedeem> {

    @FXML
    TextField accountTextField;

    @FXML
    Label checkAccountDescLabel;

    @FXML
    Button loginBtn;

    @FXML
    Button open2FAViewBtn;

    private GiftCardRedeem singleGiftCardRedeem = new GiftCardRedeem();

    /**
     * 导入账号
     */
    public void importAccountButtonAction() {
        String desc = "说明：\n" +
                "    1.格式为: 单礼品卡\n" +
                "    2.一次可以输入多条礼品卡信息，每个礼品卡单独一行。";
        openImportAccountView(List.of("giftCardCode"),"导入礼品卡",desc);
    }

    @Override
    public boolean executeButtonActionBefore() {
        if (!singleGiftCardRedeem.isLogin()){
            alert("请先登陆");
            return false;
        }
        return true;
    }

    @Override
    public void accountHandler(GiftCardRedeem giftCardRedeem) {
        giftCardRedeem.setExecTime(DateUtil.now());
        boolean success = giftCardCodeVerify(giftCardRedeem.getGiftCardCode());
        if (!success){
            throw new ServiceException("输入的代码无效。");
        }
        // X8HZ8Z7KT7DW8PML
        HttpResponse codeInfoSrvRsp = ITunesUtil.getCodeInfoSrv(singleGiftCardRedeem,giftCardRedeem.getGiftCardCode());
        String body = codeInfoSrvRsp.body();
        if (codeInfoSrvRsp.getStatus() != 200 || StrUtil.isEmpty(body)){
            throw new ServiceException("查询失败");
        }
        JSONObject bodyJSON = JSONUtil.parseObj(body);
        if (bodyJSON.getInt("status") != 0){
            throw new ServiceException("查询失败");
        }

        JSONObject codeInfo = bodyJSON.getJSONObject("codeInfo");
        giftCardRedeem.setGiftCardAmount(codeInfo.getStr("amount"));
        giftCardRedeem.setRecipientDsId(codeInfo.getStr("recipientDsId"));
        String productTypeDesc = codeInfo.getStr("productTypeDesc");
        giftCardRedeem.setGiftCardType(productTypeDesc);

        giftCardRedeem.setSalesOrg(codeInfo.getStr("salesOrg"));
        if (!StrUtil.isEmpty(productTypeDesc) && productTypeDesc.contains("-")){
            String countryCode = productTypeDesc.split("-")[1];
            String countryName = DataUtil.getNameByCountryCode(countryCode);
            if (!StrUtil.isEmpty(countryCode) && !StrUtil.isEmpty(countryName)){
                giftCardRedeem.setSalesOrg(countryCode+"-"+countryName);
            }
        }

        String status = codeInfo.getStr("status");
        if ("2".equals(status)){
            giftCardRedeem.setGiftCardStatus("未使用");
        }else if ("4".equals(status)){
            giftCardRedeem.setGiftCardStatus("已使用");
        }else{
            giftCardRedeem.setGiftCardStatus("未知");
        }
        setAndRefreshNote(giftCardRedeem,"查询成功");
    }

    /**
     * 登陆按钮点击
     */
    public void loginBtnAction(){
        new Thread(() -> {
            try{
                String accountTextFieldValue = accountTextField.getText();
                if (StrUtil.isEmpty(accountTextFieldValue)){
                    Platform.runLater(() -> checkAccountDescLabel.setText("请先输入账号信息"));
                    return;
                }
                String[] accountTextFieldValueArr = accountTextFieldValue.split("----");
                if (accountTextFieldValueArr.length != 2){
                    Platform.runLater(() -> checkAccountDescLabel.setText("账号信息格式不正确！格式：账号----密码"));
                    return;
                }

                Platform.runLater(() -> {
                    checkAccountDescLabel.setText("");
                    loginBtn.setDisable(true);
                    open2FAViewBtn.setDisable(true);
                });
                String account = accountTextFieldValueArr[0];
                String pwd     = accountTextFieldValueArr[1];
                String guid = DataUtil.getGuidByAppleId(account);
                if (StrUtil.isEmpty(singleGiftCardRedeem.getAccount())
                        || !account.equals(singleGiftCardRedeem.getAccount())
                        || !pwd.equals(singleGiftCardRedeem.getAccount())
                        || !singleGiftCardRedeem.isLogin()){
                    singleGiftCardRedeem.setAccount(account);
                    singleGiftCardRedeem.setPwd(pwd);
                    singleGiftCardRedeem.setGuid(guid);
                    singleGiftCardRedeem.setNote("");
                    itunesLogin(singleGiftCardRedeem);
                    Platform.runLater(() -> checkAccountDescLabel.setText("登陆成功..."));
                }
            }catch (ServiceException e){
                Platform.runLater(() -> checkAccountDescLabel.setText(e.getMessage()));
            }catch (Exception e){
                Platform.runLater(() -> checkAccountDescLabel.setText("数据处理异常"));
            } finally {
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    open2FAViewBtn.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * 输入双重验证码
     */
    public void open2FAViewBtnAction(){
        if (StrUtil.isEmpty(singleGiftCardRedeem.getAccount())){
            alert("请先登陆账号");
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
        loginBtnAction();
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

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        onContentMenuClick(contextMenuEvent, new ArrayList<>(new LinkedHashSet<>(){{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
        }}));
    }
}

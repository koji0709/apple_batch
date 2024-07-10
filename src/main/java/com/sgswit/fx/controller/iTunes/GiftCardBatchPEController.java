package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CommCodePopupView;
import com.sgswit.fx.controller.common.ItunesView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        // 初始化登录账号密码
        String accountText = PropertiesUtil.getOtherConfig("giftCardProAccount");
        if (!StrUtil.isEmpty(accountText)) {
            accountTextField.setText(accountText);
            loginBtnAction();
        }
        // 注册粘贴事件的监听器
        accountTextField.setOnContextMenuRequested((ContextMenuEvent event) -> {
        });
        accountTextField.setOnKeyReleased(event -> {
            if (event.isShortcutDown()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                String content = clipboard.getString().replaceAll("\t"," ");
                accountTextField.setText(content);
            }
        });
        // 设置表格cell样式
        setCellStyle();
    }

    /**
     * 设置表格样式
     */
    public void setCellStyle() {
        ObservableList<TableColumn<GiftCardRedeem, ?>> columns = accountTableView.getColumns();
        for (TableColumn<GiftCardRedeem, ?> column : columns) {
            String id = column.getId();
            if (!"seq".equals(id)){
                column.setCellFactory(col -> new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.toString());
                            GiftCardRedeem row = (GiftCardRedeem) getTableRow().getItem();
                            if (row != null) {
                                if ("giftCardType".equals(id)) {
                                    setTextFill("无效卡".equals(row.getGiftCardType()) ? Color.RED : Color.BLUE);
                                } else if ("giftCardStatus".equals(id) || "giftCardAmount".equals(id)) {
                                    setTextFill("未使用".equals(row.getGiftCardStatus()) ? Color.GREEN : Color.RED);
                                } else if ("salesOrg".equals(id)) {
                                    setTextFill(Color.BLUE);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 导入账号
     */
    public void importAccountButtonAction(ActionEvent actionEvent) {
        String desc = "说明：\n" +
                "    1.格式为: 单礼品卡\n" +
                "    2.一次可以输入多条礼品卡信息，每个礼品卡单独一行。";
        Button button = (Button) actionEvent.getSource();
        // 获取按钮所在的场景
        Scene scene = button.getScene();
        // 获取场景所属的舞台
        Stage stage = (Stage) scene.getWindow();
        openImportAccountView(List.of("giftCardCode"), "导入礼品卡", desc,stage);
    }

    @Override
    public boolean executeButtonActionBefore() {
        if (!singleGiftCardRedeem.isLogin()) {
            alert("请先登录");
            return false;
        }
        return true;
    }

    @Override
    public void accountHandler(GiftCardRedeem giftCardRedeem) {
        giftCardRedeem.setExecTime(DateUtil.now());
        String giftCardCode = giftCardRedeem.getGiftCardCode();
        boolean success = CustomStringUtils.giftCardCodeVerify(giftCardCode);
        if (!success) {
            giftCardRedeem.setGiftCardStatus("无效卡");
            throw new ServiceException("输入的代码无效。");
        }

        itunesLogin(singleGiftCardRedeem);

        // X8HZ8Z7KT7DW8PML
        HttpResponse codeInfoSrvRsp = ITunesUtil.getCodeInfoSrv(singleGiftCardRedeem, giftCardCode);
        String body = codeInfoSrvRsp.body();
        if (codeInfoSrvRsp.getStatus() != 200 || StrUtil.isEmpty(body)) {
            throw new ServiceException("查询失败");
        }
        JSONObject bodyJSON = JSONUtil.parseObj(body);
        if (bodyJSON.getInt("status") != 0) {
            throw new ServiceException("查询失败");
        }

        JSONObject codeInfo = bodyJSON.getJSONObject("codeInfo");
        giftCardRedeem.setGiftCardAmount(codeInfo.getStr("amount"));
        String recipientDsId = codeInfo.getStr("recipientDsId");
        if (StringUtils.isEmpty(recipientDsId) || "0".equals(recipientDsId)) {
            giftCardRedeem.setRecipientDsId("无(None)");
        } else {
            giftCardRedeem.setRecipientDsId(codeInfo.getStr("recipientDsId"));
        }

        String productTypeDesc = codeInfo.getStr("productTypeDesc");
        giftCardRedeem.setGiftCardType(StrUtil.isEmpty(productTypeDesc) ? "无效卡" : productTypeDesc);
        giftCardRedeem.setSalesOrg(codeInfo.getStr("salesOrg"));
        if (!StrUtil.isEmpty(productTypeDesc) && productTypeDesc.contains("-")) {
            String countryCode = productTypeDesc.split("-")[1];
            String countryName = DataUtil.getNameByCountryCode(countryCode);
            if (!StrUtil.isEmpty(countryCode) && !StrUtil.isEmpty(countryName)) {
                giftCardRedeem.setSalesOrg(countryCode + "-" + countryName);
            }
        }

        String status = codeInfo.getStr("status");
        if ("2".equals(status)) {
            giftCardRedeem.setGiftCardStatus("未使用");
            setRedeemLog(giftCardRedeem);
        } else if ("4".equals(status)) {
            if (!StringUtils.isEmpty(recipientDsId) && !"0".equals(recipientDsId)) {
                giftCardRedeem.setGiftCardStatus("旧卡(bad)");
                setRedeemLog(giftCardRedeem);
            } else {
                giftCardRedeem.setGiftCardStatus("已使用");
                setRedeemLog(giftCardRedeem);
            }
        } else {
            giftCardRedeem.setGiftCardStatus("无效卡");
        }
        setAndRefreshNote(giftCardRedeem, "查询成功");
    }

    private void setRedeemLog(GiftCardRedeem giftCardRedeem) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", giftCardRedeem.getGiftCardCode());
        HttpResponse giftcardRedeemLogRsp = HttpUtils.get("/giftcardRedeemLog", params);
        boolean verify = HttpUtils.verifyRsp(giftcardRedeemLogRsp);
        if (verify) {
            JSONArray dataList = HttpUtils.dataList(giftcardRedeemLogRsp);
            if (!CollUtil.isEmpty(dataList)) {
                List<String> redeemList = new ArrayList<>();
                String format = "此代码已被dsid[%s]在%s兑换";
                for (Object o : dataList) {
                    JSONObject json = (JSONObject) o;
                    redeemList.add(String.format(format, json.getStr("recipientDsid"), json.getStr("redeemTime")));
                }
                giftCardRedeem.setRedeemLog(CollUtil.join(redeemList, ";"));
            } else {
                giftCardRedeem.setRedeemLog("在本平台暂无兑换记录。");
            }
        } else {
            giftCardRedeem.setRedeemLog("-");
        }
    }

    /**
     * 登录按钮点击
     */
    public void loginBtnAction() {
        ThreadUtil.execAsync(() -> {
            try {
                String accountTextFieldValue = accountTextField.getText();
                if (StrUtil.isEmpty(accountTextFieldValue)) {
                    Platform.runLater(() -> checkAccountDescLabel.setText("请先输入账号信息"));
                    return;
                }
                String[] accountTextFieldValueArr = AccountImportUtil.parseAccountAndPwd(accountTextFieldValue);
                if (accountTextFieldValueArr.length != 2) {
                    Platform.runLater(() -> checkAccountDescLabel.setText("账号信息格式不正确！格式：账号----密码"));
                    return;
                }

                Platform.runLater(() -> {
                    checkAccountDescLabel.setText("正在登录...");
                    loginBtn.setDisable(true);
                    open2FAViewBtn.setDisable(true);
                });
                String account = accountTextFieldValueArr[0];
                String pwd = accountTextFieldValueArr[1];
                String guid = DataUtil.getGuidByAppleId(account);
                if (StrUtil.isEmpty(singleGiftCardRedeem.getAccount())
                        || !account.equals(singleGiftCardRedeem.getAccount())
                        || !pwd.equals(singleGiftCardRedeem.getAccount())
                        || !singleGiftCardRedeem.isLogin()) {
                    singleGiftCardRedeem.setAccount(account);
                    singleGiftCardRedeem.setPwd(pwd);
                    singleGiftCardRedeem.setGuid(guid);
                    singleGiftCardRedeem.setNote("");
                    itunesLogin(singleGiftCardRedeem);

                    Platform.runLater(() -> {
                        checkAccountDescLabel.setTextFill(Paint.valueOf("#169bd5"));
                        checkAccountDescLabel.setText("登录成功[dsid：" + singleGiftCardRedeem.getDsPersonId() + "],已开启自动登录");
                        PropertiesUtil.setOtherConfig("giftCardProAccount", accountTextFieldValue);
                    });
                }
            }catch (IORuntimeException e) {
                Platform.runLater(() -> {
                    checkAccountDescLabel.setTextFill(Paint.valueOf("#ff0000"));
                    checkAccountDescLabel.setText("连接异常，请检查网络");
                });
            } catch (ServiceException e) {
                Platform.runLater(() -> {
                    checkAccountDescLabel.setTextFill(Paint.valueOf("#ff0000"));
                    checkAccountDescLabel.setText(e.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    checkAccountDescLabel.setTextFill(Paint.valueOf("#ff0000"));
                    checkAccountDescLabel.setText("数据处理异常");
                });
            } finally {
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    open2FAViewBtn.setDisable(false);
                });
            }
        });
    }

    /**
     * 输入双重验证码
     */
    public void open2FAViewBtnAction() {
        if (StrUtil.isEmpty(singleGiftCardRedeem.getAccount())) {
            alert("请先登录账号");
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
        if (StringUtils.isEmpty(code)) {
            return;
        }
        singleGiftCardRedeem.setAuthCode(code);
        loginBtnAction();
    }



    @Override
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        onContentMenuClick(contextMenuEvent, new ArrayList<>(new LinkedHashSet<>() {{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
        }}));
    }
    @Override
    public List<GiftCardRedeem> parseAccount(String accountStr){
        List<GiftCardRedeem> accountList = new ArrayList<>();
        String[] accList = accountStr.split("\n");
        for (int i = 0; i < accList.length; i++) {
            String acc = accList[i];
            if (!StrUtil.isEmpty(acc)){
                String accountString = accountTextField.getText();
                if (!StrUtil.isEmpty(accountStr)){
                    String[] accountArr = AccountImportUtil.parseAccountAndPwd(accountString);
                    if (accountArr.length != 2){
                        continue;
                    }else{
                        GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
                        giftCardRedeem.setAccount(accountArr[0]);
                        giftCardRedeem.setPwd(accountArr[1]);
                        String giftCardCode=CustomStringUtils.replaceMultipleSpaces(acc,"");
                        giftCardRedeem.setGiftCardCode(giftCardCode);
                        boolean success = CustomStringUtils.giftCardCodeVerify(giftCardCode);
                        if (!success){
                            giftCardRedeem.setGiftCardStatus("无效卡");
                        }
                        accountList.add(giftCardRedeem);
                    }
                }
            }

        }
        return accountList;
    }
}

package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.constant.StoreFontsUtils;
import com.sgswit.fx.controller.common.ItunesView;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.controller.exception.TwoFactorAuthenticationException;
import com.sgswit.fx.controller.query.WhetherAppleIdController;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.OcrUtil;
import com.sgswit.fx.utils.PListUtil;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.StrUtils;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CheckBalanceDisabledController extends ItunesView<Account> {
    @FXML
    public CheckBox apiCheckBox;
    @FXML
    public TextField apiYcTextField;
    @FXML
    public TextField apiCsTextField;
    @FXML
    public TextField apiDmTextField;

    private String regex = "(\\d{6})";
    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction(ActionEvent actionEvent) {
        openImportAccountView(List.of("account----pwd","account----pwd-api"),actionEvent);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        apiCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                apiYcTextField.setDisable(false);
                apiCsTextField.setDisable(false);
                apiDmTextField.setDisable(false);
            } else {
                apiYcTextField.setDisable(true);
                apiCsTextField.setDisable(true);
                apiDmTextField.setDisable(true);
            }
        });
    }

    @Override
    public Long getIntervalFrequency() {
        return 500L;
    }

    public Boolean apple(Account account, int tryNumber) {
        if (tryNumber >= 15) {
            throw new ServiceException("查询失败：图片验证码解析失败");
        }

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.9"));

        String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
        HttpResponse captchaResponse = ProxyUtil.execute(HttpUtil.createGet(url).header(headers));
        String body = captchaResponse.body();

        if (StrUtil.isEmpty(body) || StrUtils.isEmptyJsonObject(body)) {
            return apple(account, ++tryNumber);
        }

        try {
            JSONObject object = JSONUtil.parseObj(body);
            String capId = object.getStr("id");
            String capToken = object.getStr("token");
            ThreadUtil.sleep(1 * 1000);
            JSONObject payloadJson = JSONUtil.parseObj(object.getStr("payload"));
            String content = payloadJson.getStr("content");
            String predict = OcrUtil.recognize(content);

            String requestBody = "{\"id\":\"" + account.getAccount() + "\",\"captcha\":{\"id\":" + capId + ",\"answer\":\"" + predict + "\",\"token\":\"" + capToken + "\"}}\n";
            HttpResponse verifyAppleIdRes = ProxyUtil.execute(
                    HttpUtil.createPost("https://iforgot.apple.com/password/verify/appleid")
                            .body(requestBody)
                            .header(headers)
            );

            // 简化逻辑：先检查重定向成功的情况
            if (verifyAppleIdRes.getStatus() == 302) {
                String location = verifyAppleIdRes.header("Location");
                if (StrUtil.containsIgnoreCase(location, "/password/verify/phone")) {
                    return true; // 成功
                }
                // 其他重定向情况，重试
                return apple(account, ++tryNumber);
            }

            // 处理非302状态码
            if (verifyAppleIdRes.getStatus() == 400) {
                String serviceErrors = JSONUtil.parse(verifyAppleIdRes.body()).getByPath("service_errors", String.class);
                JSONArray jsonArray = JSONUtil.parseArray(serviceErrors);
                if (jsonArray != null && jsonArray.size() > 0) {
                    String code = JSONUtil.parseObj(jsonArray.get(0)).getStr("code");
                    if ("captchaAnswer.Invalid".equals(code)) {
                        ThreadUtil.sleep(500);
                        return apple(account, ++tryNumber); // 验证码错误，重试
                    }
                }
                return false; // 其他400错误，返回失败
            }

            // 其他状态码（200、500等），重试
            return apple(account, ++tryNumber);

        } catch (Exception e) {
            return apple(account, ++tryNumber);
        }
    }

    /**
     * 账号处理
     */
    @Override
    public void accountHandler(Account account) {
        setAndRefreshNote(account,"登录查询中...");
        //查看是否需要api接码
        boolean selected = apiCheckBox.isSelected();
        if(selected){
            if(StrUtil.isEmpty(apiCsTextField.getText()) || !NumberUtil.isInteger(apiCsTextField.getText())){
                apiCsTextField.setText("15");
            }
            if(StrUtil.isEmpty(apiYcTextField.getText()) || !NumberUtil.isInteger(apiYcTextField.getText())){
                apiCsTextField.setText("5");
            }
        }
        String id=super.createId(account.getAccount(),account.getPwd());
        loginSuccessMap.remove(id);
        try {
            itunesLogin(account);
        }catch (TwoFactorAuthenticationException e){
            Boolean hasTwoFactorAuthentication = apple(account,0);
            if(selected && hasTwoFactorAuthentication){
                setAndRefreshNote(account, "成功发送验证码，正在解析验证码...");
                // 循环查询验证码
                Integer cs = Integer.valueOf(apiCsTextField.getText());
                //获取接码
                String api = account.getApi();
                if (StrUtil.isEmpty(api)) {
                    throw new ServiceException("解析失败，未绑定接码的API地址。");
                }
                String smsCode = "";
                for (int i = 1; i <= cs; i++) {
                    try {
                        // 暂停5秒（5000毫秒）
                        Integer yc = Integer.valueOf(apiYcTextField.getText());
                        Thread.sleep(yc * 1000);
                        setAndRefreshNote(account,"第 " + (i) + " 次获取验证码");
                        HttpResponse httpResponse = HttpRequest.get(api).execute();
                        if(httpResponse.getStatus()==200){
                            String responseBody = httpResponse.body();
                            if (StrUtil.isNotEmpty(responseBody)) {
                                Pattern compile = Pattern.compile(StrUtil.isEmpty(apiDmTextField.getText())?regex:apiDmTextField.getText(),Pattern.CASE_INSENSITIVE);
                                Matcher matcher = compile.matcher(responseBody);
                                // 查找并提取匹配的数字
                                if (matcher.find()) {
                                    smsCode = matcher.group();
                                    break;
                                }
                            }
                        }
                    } catch (InterruptedException ae) {
                        // 恢复中断状态（可选，根据业务需求）
                        Thread.currentThread().interrupt();
                        // 若中断，可提前退出循
                    }
                }
                if(!StrUtil.isEmpty(smsCode)){
                    setAndRefreshNote(account, "已成功接收验证码，正在查询。");
                    secondStepHandler(account, smsCode);
                    return;
                }else{
                    throw new ServiceException("查询失败：验证码解析失败");
                }
            }else {
                throw e;
            }
        }catch (ServiceException e){
            throw e;
        }
        HttpResponse authRsp = (HttpResponse)account.getAuthData().get("authRsp");
        JSONObject rspJSON = PListUtil.parse(authRsp.body());
        String balance  = rspJSON.getStr("creditDisplay","0");
        Boolean isDisabledAccount  = rspJSON.getByPath("accountFlags.isDisabledAccount",Boolean.class);
        account.setBalance((StrUtil.isEmpty(balance) ? "0" : balance));
        account.setDisableStatus( !isDisabledAccount ? "正常" : "禁用");
        String storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
        String country = StoreFontsUtils.getCountryCode(StrUtil.split(storeFront, "-").get(0));
        if (!StrUtil.isEmpty(country)){
            String[] sp = country.split("-");
            account.setAreaCode(sp[0]);
            account.setArea(sp[1]);
        }
        account.setAreaId(account.getItspod());
        setAndRefreshNote(account,"查询成功");
    }

    @Override
    protected void secondStepHandler(Account account, String code) {
        account.setAuthCode(code);
        accountHandlerExpand(account);
    }

    public void onPrompt(MouseEvent mouseEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setPrefHeight(400);
        alert.getDialogPane().setPrefWidth(800);
        alert.getDialogPane().setStyle(" -fx-font-size: 16px;");
        alert.setTitle("提示");
        alert.setContentText("Api读取验证码配置说明：\n" +
                "Api读取验证码支持字段或者正则提取验证码，多个关键字按|分割\n" +
                "例如: api请求返回： {\"code\":1,\"msg\":\"ok\",\"data\":{\"code\":\"Your Apple Account Code is: 583173. Don't share it with anyone.\",\"code_time\":\"2025-11-03 14:44:01\",\"expired_date\":\"2025-11-20\"}}\n" +
                "验证码是 583173,字段是 code。\n" +
                "\n" +
                "正则方式 提取六位整数：则 APi配置填: \\\\d{6}\n" +
                "正则方式 限定上下文：匹配is: 后的 6 位数字：则 APi配置填: is: (\\\\d{6})\n" +
                "正则方式 更严谨：结合前后文边界（避免部分匹配）则 APi配置填: is: (\\\\d{6})\\\\.\n");

        alert.showAndWait();
    }

}

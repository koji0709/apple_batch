package com.sgswit.fx.controller.query;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.OcrUtil;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.ContextMenuEvent;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
/**
 * <p>
 *  急速过滤密正
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class RapidFiltrationController extends CustomTableView<Account> {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.RAPID_FILTRATION.getCode())));
        super.initialize(url, resourceBundle);
    }
    public List<String> menuItem =new ArrayList<>(){{
        add(Constant.RightContextMenu.DELETE.getCode());
        add(Constant.RightContextMenu.REEXECUTE.getCode());
        add(Constant.RightContextMenu.COPY.getCode());
    }};

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItem,new ArrayList<>());
    }

    @FXML
    public void onAccountInputBtnClick(ActionEvent actionEvent){
        openImportAccountView(List.of("account----pwd"),actionEvent);
    }


    @Override
    public void accountHandler(Account account) {
        //扣除点数
        try {
            account.setHasFinished(false);
            setAndRefreshNote(account,"正在获取验证码...");
            ThreadUtil.sleep(1000);
            HashMap<String, List<String>> headers = new HashMap<>();
            headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
            headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
            String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
            HttpResponse captchaResponse =
                    ProxyUtil.execute(HttpUtil.createGet(url)
                            .header(headers));
            setAndRefreshNote(account,"正在识别验证码...");
            String body = captchaResponse.body();
            JSONObject object = JSONUtil.parseObj(body);
            String capId = object.getStr("id");
            String capToken = object.getStr("token");
            JSONObject payloadJson = JSONUtil.parseObj(JSONUtil.parseObj(body).getStr("payload"));
            String content = payloadJson.getStr("content");
            String predict = OcrUtil.recognize(content);
            setAndRefreshNote(account,"正在验证账户...");
            String bodys = "{\"id\":\"" + account.getAccount() + "\",\"captcha\":{\"id\":" + capId + ",\"answer\":\"" + predict + "\",\"token\":\"" + capToken + "\"}}\n";
            HttpResponse verifyAppleIdRes = ProxyUtil.execute(HttpUtil.createPost("https://iforgot.apple.com/password/verify/appleid")
                    .body(bodys)
                    .header(headers));
            if(verifyAppleIdRes.getStatus() == 400){
                JSONObject jsonObject = JSONUtil.parseObj(verifyAppleIdRes.body());
                boolean hasError = jsonObject.getBool("hasError");
                if(hasError){
                    String service_errors = jsonObject.getStr("service_errors");
                    JSONArray jsonArray = JSONUtil.parseArray(service_errors);
                    String code = JSONUtil.parseObj(jsonArray.get(0)).getStr("code");
                    if("captchaAnswer.Invalid".equals(code)){
                        //返还点数
                        PointUtil.pointCost(FunctionListEnum.WHETHER_APPLEID.getCode(),PointUtil.in,account.getAccount());
                        accountHandler(account);
                    }else{
                        String message = JSONUtil.parseObj(jsonArray.get(0)).getStr("message");
                        setAndRefreshNote(account,message);
                    }
                }
            }else if(verifyAppleIdRes.getStatus() == 302){
                String location=verifyAppleIdRes.header("Location");
                if(StringUtils.containsIgnoreCase(location,"password/authenticationmethod")){
                    setAndRefreshNote(account,"此AppleID已被锁定");
                }else if(StringUtils.containsIgnoreCase(location,"recovery/options")){
                    setAndRefreshNote(account,"登录中...");
                    HttpResponse step1Res = AppleIDUtil.signin(account);
                    if (step1Res.getStatus() != 409) {
                        String service_errors = JSONUtil.parseObj(verifyAppleIdRes.body()).getStr("serviceErrors");
                        JSONArray jsonArray = JSONUtil.parseArray(service_errors);
                        if(null!=jsonArray){
                            String message = JSONUtil.parseObj(jsonArray.get(0)).getStr("message");
                            setAndRefreshNote(account,message);
                        }
                    }else{
                        setAndRefreshNote(account,"正常账号");
                    }
                }else if(StringUtils.containsIgnoreCase(location,"password/verify/phone")){
                    setAndRefreshNote(account,"登录中...");
                    HttpResponse step1Res = AppleIDUtil.signin(account);
                    if (step1Res.getStatus() != 409) {
                        String service_errors = JSONUtil.parseObj(verifyAppleIdRes.body()).getStr("serviceErrors");
                        JSONArray jsonArray = JSONUtil.parseArray(service_errors);
                        if(null!=jsonArray){
                            String message = JSONUtil.parseObj(jsonArray.get(0)).getStr("message");
                            setAndRefreshNote(account,message);
                        }
                    }else{
                        setAndRefreshNote(account,"此AppleID已开启双重认证");
                    }
                }
                insertLocalHistory(List.of(account));
            }else if(verifyAppleIdRes.getStatus() == 200){
                JSONObject jsonObject = JSONUtil.parseObj(verifyAppleIdRes.body());
                String service_errors = jsonObject.getStr("serviceErrors");
                JSONArray jsonArray = JSONUtil.parseArray(service_errors);
                if(null!=jsonArray){
                    String message = JSONUtil.parseObj(jsonArray.get(0)).getStr("message");
                    setAndRefreshNote(account,message);
                }
            }
        }catch (Exception e){

        }finally {
            account.setHasFinished(true);
        }

    }

}

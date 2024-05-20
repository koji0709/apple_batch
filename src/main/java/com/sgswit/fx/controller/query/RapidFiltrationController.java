package com.sgswit.fx.controller.query;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.UnavailableException;
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
            setAndRefreshNote(account,"查询中...");
            Thread.sleep(2*1000);
            HashMap<String, List<String>> headers = new HashMap<>();
            headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
            headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
            String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
            HttpResponse captchaResponse =
                    ProxyUtil.execute(HttpUtil.createGet(url)
                            .header(headers));
            if(captchaResponse.getStatus()==503){
                throw new UnavailableException();
            }else{
                String body = captchaResponse.body();
                JSONObject object = JSONUtil.parseObj(body);
                String capId = object.getStr("id");
                String capToken = object.getStr("token");
                //解析图片
                try {
                    Thread.sleep(2*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                JSONObject payloadJson = JSONUtil.parseObj(JSONUtil.parseObj(body).getStr("payload"));
                String content = payloadJson.getStr("content");
                String predict = OcrUtil.recognize(content);
                String bodys = "{\"id\":\"" + account.getAccount() + "\",\"captcha\":{\"id\":" + capId + ",\"answer\":\"" + predict + "\",\"token\":\"" + capToken + "\"}}\n";
                HttpResponse verifyAppleIdRes = ProxyUtil.execute(HttpUtil.createPost("https://iforgot.apple.com/password/verify/appleid")
                                .body(bodys)
                                .header(headers));
                if (verifyAppleIdRes.getStatus() == 503) {
                    account.setFailCount(account.getFailCount()+1);
                    if(account.getFailCount() >= 5){
                        throw new UnavailableException();
                    }else{
                        //返还点数
                        PointUtil.pointCost(FunctionListEnum.WHETHER_APPLEID.getCode(),PointUtil.in,account.getAccount());
                    }
                    try {
                        Thread.sleep(10*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    accountHandler(account);
                }else if(verifyAppleIdRes.getStatus() == 400){
                    JSONObject jsonObject = JSONUtil.parseObj(verifyAppleIdRes.body());
                    boolean hasError = jsonObject.getBool("hasError");
                    if(hasError){
                        String service_errors = jsonObject.getStr("service_errors");
                        JSONArray jsonArray = JSONUtil.parseArray(service_errors);
                        String code = JSONUtil.parseObj(jsonArray.get(0)).getStr("code");
                        if(code.equals("captchaAnswer.Invalid")){
                            //返还点数
                            PointUtil.pointCost(FunctionListEnum.WHETHER_APPLEID.getCode(),PointUtil.in,account.getAccount());
                            accountHandler(account);
                        }else if(code.equals("-20210")){
                            setAndRefreshNote(account,"这个 Apple ID 没有被激活。");
                            insertLocalHistory(List.of(account));
                        }
                    }
                }else if(verifyAppleIdRes.getStatus() == 302){
                    String location=verifyAppleIdRes.header("Location");
                    if(StringUtils.containsIgnoreCase(location,"password/authenticationmethod")){
                        setAndRefreshNote(account,"此AppleID已被锁定");
                    }else if(StringUtils.containsIgnoreCase(location,"recovery/options")){
                        setAndRefreshNote(account,"登录中...");
                        HttpResponse step1Res = AppleIDUtil.signin(account);
                        if(step1Res.getStatus() == 503){
                            throw new UnavailableException();
                        } else if (step1Res.getStatus() != 409) {
                            setAndRefreshNote(account,"Apple ID 或密码不正确");
                        }else{
                            setAndRefreshNote(account,"正常账号");
                        }
                    }else if(StringUtils.containsIgnoreCase(location,"password/verify/phone")){
                        setAndRefreshNote(account,"登录中...");
                        HttpResponse step1Res = AppleIDUtil.signin(account);
                        if(step1Res.getStatus() == 503){
                            throw new UnavailableException();
                        } else if (step1Res.getStatus() != 409) {
                            setAndRefreshNote(account,"Apple ID 或密码不正确");
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
                        String code = JSONUtil.parseObj(jsonArray.get(0)).getStr("code");
                        if(code.equals("appleIdNotSupported")){
                            setAndRefreshNote(account,"此 Apple ID 无效或不受支持。");
                            insertLocalHistory(List.of(account));
                        }
                    }
                }
            }
        }catch (Exception e){

        }finally {
            account.setHasFinished(true);
        }

    }

}

package com.sgswit.fx.controller.query;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.controller.exception.UnavailableException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.web.AppleIDUtil;
import javafx.event.ActionEvent;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * <p>
 * 检测是否AppleID
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class WhetherAppleIdController extends CustomTableView<Account> {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.WHETHER_APPLEID.getCode())));
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
    public void openImportAccountView(ActionEvent actionEvent){
        openImportAccountView(List.of("account"),actionEvent);
    }

    @Override
    public void accountHandler(Account account) {
        //扣除点数
        try {
            account.setHasFinished(false);
            HttpResponse verifyAppleIdRes = AppleIDUtil.captchaAndVerifyPost(account);
            if (verifyAppleIdRes.getStatus() != 302) {
                throw new ServiceException("验证码自动识别失败");
            }
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
                if(StrUtil.containsIgnoreCase(location,"password/authenticationmethod") || StrUtil.containsIgnoreCase(location,"/account/inactive")){
                    setAndRefreshNote(account,"此AppleID已被锁定");
                }else if(StrUtil.containsIgnoreCase(location,"recovery/options")){
                    setAndRefreshNote(account,"此AppleID正常");
                }else if(StrUtil.containsIgnoreCase(location,"password/verify/phone")){
                    setAndRefreshNote(account,"此AppleID已开启双重认证");
                }
            }else if(verifyAppleIdRes.getStatus() == 200){
                JSONObject jsonObject = JSONUtil.parseObj(verifyAppleIdRes.body());
                String service_errors = jsonObject.getStr("serviceErrors");
                JSONArray jsonArray = JSONUtil.parseArray(service_errors);
                if(null!=jsonArray){
                    String message = JSONUtil.parseObj(jsonArray.get(0)).getStr("message");
                    setAndRefreshNote(account,message);
                }
            }
        }catch (UnavailableException e){
            throw e;
        }catch (Exception e){
            throw e;
        }finally {
            account.setHasFinished(true);
        }

    }
}

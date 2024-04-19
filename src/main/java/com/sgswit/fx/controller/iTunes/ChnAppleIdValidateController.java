package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.map.MapUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.UserInfo;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.PurchaseBillUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.ContextMenuEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author DeZh
 * @title: ChnAppleIdValidateController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/219:01
 */
public class ChnAppleIdValidateController extends CustomTableView<UserInfo>{
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.CHN_APPLE_ID_VALIDATE.getCode())));
        super.initialize(url,resourceBundle);
    }

    @FXML
    protected void onAccountInputBtnClick(ActionEvent actionEvent) throws IOException {
        super.openImportAccountView(List.of("account----pwd----name----nationalId----phone"),actionEvent);
    }

    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }
    /**
     　* 双重验证
     * @param
     * @param account
     * @param authCode
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/12/23 18:10
     */
    @Override
    protected void twoFactorCodeExecute(UserInfo account, String authCode){
        try{
            Map<String,Object> res=account.getAuthData();
            if(Constant.TWO_FACTOR_AUTHENTICATION.equals(MapUtil.getStr(res,"code"))){
                account.setAuthCode(authCode);
                account.setStep("00");
                accountHandlerExpand(account);
            }else{
                alert("未下发双重验证码");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void accountHandler(UserInfo account){
//        account.setHasFinished(false);
        setAndRefreshNote(account,"正在登录...");
        String step= StringUtils.isEmpty(account.getStep())?"01":account.getStep();
        Map<String,Object> accountInfoMap=account.getAuthData();
        if("00".equals(step)){
            String authCode=account.getAuthCode();
            accountInfoMap= PurchaseBillUtil.iTunesAuth(authCode,accountInfoMap);
        }else if("01".equals(step)){
            accountInfoMap= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
        }else{
            accountInfoMap=new HashMap<>();
        }
        if(Constant.TWO_FACTOR_AUTHENTICATION.equals(accountInfoMap.get("code"))) {
            account.setAuthData(accountInfoMap);
            throw new ServiceException(String.valueOf(accountInfoMap.get("msg")));
        }else if(!Constant.SUCCESS.equals(accountInfoMap.get("code"))){
            throw new ServiceException(String.valueOf(accountInfoMap.get("msg")));
        }else {
            boolean hasInspectionFlag= (boolean) accountInfoMap.get("hasInspectionFlag");
            if(!hasInspectionFlag){
                throw new ServiceException("此 Apple ID 尚未用于 App Store。");
            }
            setAndRefreshNote(account,"登录成功，正在校验身份信息...");
            //判断是否需要认证
            boolean f= ITunesUtil.redeemLandingPage(accountInfoMap);
            if(f){
                accountInfoMap.put("name",account.getName());
                accountInfoMap.put("phone",account.getPhone());
                accountInfoMap.put("nationalId", account.getNationalId());
                accountInfoMap=ITunesUtil.redeemValidateId(accountInfoMap);
                setAndRefreshNote(account, MapUtil.getStr(accountInfoMap, "msg") );
                account.setDataStatus(Constant.SUCCESS.equals(MapUtil.getStr(accountInfoMap, "code"))?"1":"0");
            }else{
                account.setHasFinished(true);
                account.setDataStatus("1");
                setAndRefreshNote(account, "该账户不需要认证。");
                return;
            }
        }
        account.setHasFinished(true);
        accountTableView.refresh();
    }
}

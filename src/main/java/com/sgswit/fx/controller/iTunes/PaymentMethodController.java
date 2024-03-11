package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.map.MapUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
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
　* iTunes付款方式管理
  * @param
　* @throws
　* @author DeZh
　* @date 2023/10/27 10:10
 */
public class PaymentMethodController extends CustomTableView<Account>{
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.DELETE_PAYMENT.getCode())));
        super.initialize(url,resourceBundle);
    }


    @FXML
    protected void onAccountInputBtnClick(ActionEvent actionEvent) throws IOException {
        openImportAccountView(List.of("account----pwd"),actionEvent);
    }
    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }
    @Override
    public void accountHandler(Account account){
        setAndRefreshNote(account, "正在登录...");
        String step= StringUtils.isEmpty(account.getStep())?"01":account.getStep();
        Map<String,Object> res=account.getAuthData();
        if("00".equals(step)){
            String authCode=account.getAuthCode();
            res= PurchaseBillUtil.iTunesAuth(authCode,res);
        }else if("01".equals(step)){
            res= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
        }else{
            res=new HashMap<>();
        }
        if(Constant.TWO_FACTOR_AUTHENTICATION.equals(res.get("code"))) {
            account.setNote(String.valueOf(res.get("msg")));
            account.setAuthData(res);
        }else if(!res.get("code").equals(Constant.SUCCESS)){
            account.setDataStatus("0");
            account.setNote(res.get("msg").toString());
            setAndRefreshNote(account, res.get("msg").toString());
        }else{
            boolean hasInspectionFlag= (boolean) res.get("hasInspectionFlag");
            if(!hasInspectionFlag){
                account.setHasFinished(true);
                account.setDataStatus("0");
                setAndRefreshNote(account,"此 Apple ID 尚未用于 App Store。");
                return;
            }
            setAndRefreshNote(account, "登录成功，数据删除中...");
            res=ITunesUtil.delPaymentInfos(res);
            if(res.get("code").equals(Constant.SUCCESS)){
                account.setDataStatus("1");
                tableRefreshAndInsertLocal(account, MapUtil.getStr(res,"msg"));
            }else{
                account.setDataStatus("0");
                tableRefreshAndInsertLocal(account, MapUtil.getStr(res,"msg"));
            }

        }
        account.setHasFinished(true);
        accountTableView.refresh();
    }
    @Override
    protected void twoFactorCodeExecute(Account account, String authCode){
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
}

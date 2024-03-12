package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.map.MapUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.CreditCard;
import com.sgswit.fx.utils.*;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
public class BindVirtualCardController extends CustomTableView<CreditCard>{
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.BIND_VIRTUAL_CARD.getCode())));
        super.initialize(url,resourceBundle);
    }


    @FXML
    protected void onAccountInputBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/virtualCard-input-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 300);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("信用卡导入");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();

        VirtualCardInputPopupController c = fxmlLoader.getController();
        if(null == c.getData() || "".equals(c.getData())){
            return;
        }
        String[] lineArray = c.getData().split("\n");
        String account="";
        String pwd="";
        for(String item : lineArray){
            boolean f=false;
            //判断是否符合正则表达式
            CreditCard creditCard = new CreditCard();
            item= CustomStringUtils.replaceMultipleSpaces(item, AccountImportUtil.SPLIT_STRING);
            String[]  array=item.split(AccountImportUtil.SPLIT_STRING);
            if(array.length==2){
                account=array[0];
                pwd=array[1].replace("{-}", AccountImportUtil.REPLACE_MEANT);
            }else if(array.length>2){
                account=array[0];
                pwd=array[1].replace("{-}", AccountImportUtil.REPLACE_MEANT)+" "+array[2].replace("{-}", AccountImportUtil.REPLACE_MEANT);
            }else{
                boolean isEmailStarted=AccountImportUtil.checkIfEmailStarted(item);
                if(isEmailStarted){
                    account=AccountImportUtil.getEmailByStr(item);
                    pwd= item.substring(item.lastIndexOf(account)+account.length()).replace("{-}", AccountImportUtil.REPLACE_MEANT);
                    pwd=StringUtils.replacePattern(pwd, "-| ", " ").trim();
                    pwd= CustomStringUtils.replaceMultipleSpaces(pwd,AccountImportUtil.SPLIT_STRING).replace(AccountImportUtil.REPLACE_MEANT,"-");
                }else{
                    item=item.replace("{-}", AccountImportUtil.REPLACE_MEANT);
                    item= StringUtils.replacePattern(item, "-| ", " ").trim();
                    item= CustomStringUtils.replaceMultipleSpaces(item,AccountImportUtil.SPLIT_STRING);
                    String []accountArr=item.split(AccountImportUtil.SPLIT_STRING,2);
                    account=accountArr[0];
                    if(accountArr.length>1){
                        pwd=accountArr[1];
                    }
                }
            }
            pwd=StringUtils.replacePattern(pwd, "-| ", " ").trim();
            pwd= CustomStringUtils.replaceMultipleSpaces(pwd,AccountImportUtil.SPLIT_STRING).replace(AccountImportUtil.REPLACE_MEANT,"-");
            if(!StringUtils.isEmpty(pwd)){
                String[] a=pwd.split(AccountImportUtil.SPLIT_STRING);
                if(a.length==2){
                    creditCard.setSeq(accountList.size()+1);
                    creditCard.setAccount(account);
                    creditCard.setPwd(a[0]);
                    creditCard.setCreditInfo(a[1]);
                    String cCreditInfoRegex = "\\w{1,40}/\\d{6}/\\w{3}";
                    if(a[1].matches(cCreditInfoRegex)){
                        f=true;
                        String[] creditInfoArr=a[1].split("/");
                        creditCard.setCreditCardNumber(creditInfoArr[0]);
                        creditCard.setCreditVerificationNumber(creditInfoArr[2]);
                        String monthAndYear=creditInfoArr[1];
                        creditCard.setCreditCardExpirationMonth(Integer.valueOf(monthAndYear.substring(0,2)).toString());
                        creditCard.setCreditCardExpirationYear(monthAndYear.substring(2));
                    }
                }
            }

            if(f){
                accountList.add(creditCard);
            }
        }
        accountTableView.setItems(accountList);
        setAccountNumLabel();
        Button button = (Button) actionEvent.getSource();
        // 获取按钮所在的场景
        Scene parentScene = button.getScene();
        // 获取场景所属的舞台
        Stage stage = (Stage) parentScene.getWindow();
        stage.show();
    }

    @Override
    public void accountHandler(CreditCard account){
        account.setHasFinished(false);
        setAndRefreshNote(account,"正在登录...");
        String step= StringUtils.isEmpty(account.getStep())?"01":account.getStep();
        Map<String,Object> res=account.getAuthData();
        if("01".equals(step)){
            res= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
        }else if("02".equals(step)){
            res.put("smsCode",account.getAuthCode());
        }else{
            res=new HashMap<>();
        }

        res.put("creditCardNumber",account.getCreditCardNumber());
        res.put("creditCardExpirationMonth",account.getCreditCardExpirationMonth());
        res.put("creditCardExpirationYear",account.getCreditCardExpirationYear());
        res.put("creditVerificationNumber",account.getCreditVerificationNumber());
        if(Constant.TWO_FACTOR_AUTHENTICATION.equals(res.get("code"))) {
            account.setNote(String.valueOf(res.get("msg")));
            account.setAuthData(res);
        }else if(!Constant.SUCCESS.equals(res.get("code"))){
            account.setDataStatus("0");
            setAndRefreshNote(account, String.valueOf(res.get("msg")));
        }else{
            boolean hasInspectionFlag= (boolean) res.get("hasInspectionFlag");
            if(!hasInspectionFlag){
                account.setDataStatus("0");
                setAndRefreshNote(account, "此 Apple ID 尚未用于 App Store。");
                return;
            }
            setAndRefreshNote(account,"登录成功，正在验证银行卡信息...");
            Map<String,Object> addCreditPaymentRes=ITunesUtil.addCreditPayment(res,step);
            if(Constant.SUCCESS.equals(addCreditPaymentRes.get("code")) && "01".equals(step)){
                Map<String,Object> data=MapUtil.get(addCreditPaymentRes,"data",Map.class);
                account.setAuthData(res);
            }else{
                if(Constant.SUCCESS.equals(MapUtil.getStr(addCreditPaymentRes,"code"))){
                    account.setDataStatus("1");
                }else{
                    account.setDataStatus("0");
                }
                setAndRefreshNote(account, MapUtil.getStr(addCreditPaymentRes,"message"));
            }
            account.setNote(MapUtil.getStr(addCreditPaymentRes,"message"));
        }
        account.setHasFinished(true);
        accountTableView.refresh();
    }


   /**第二步操作**/
    @Override
    protected void secondStepHandler(CreditCard account, String code){
        account.setAuthCode(code);
        account.setStep("02");
        if (StringUtils.isEmpty(code)){
            return;
        }
        String step= StringUtils.isEmpty(account.getStep())?"01":account.getStep();
        Map<String,Object> res=new HashMap<>();
        if(step.equals("02")){
            res=account.getAuthData();
            if(null==res){
                return;
            }
            res.put("smsCode",account.getAuthCode());
        }else{
            res= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
        }

        res.put("creditCardNumber",account.getCreditCardNumber());
        res.put("creditCardExpirationMonth",account.getCreditCardExpirationMonth());
        res.put("creditCardExpirationYear",account.getCreditCardExpirationYear());
        res.put("creditVerificationNumber",account.getCreditVerificationNumber());
        Map<String,Object> addCreditPaymentRes=ITunesUtil.addCreditPayment(res,step);
        if(addCreditPaymentRes.get("code").equals(Constant.SUCCESS) && "01".equals(step)){
            addCreditPaymentRes.get("data");
            account.setAuthData((ObservableMap<String, Object>) addCreditPaymentRes.get("data"));
        }else{

        }
        account.setNote(MapUtil.getStr(addCreditPaymentRes,"message"));
        accountTableView.refresh();
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
    protected void twoFactorCodeExecute(CreditCard account, String authCode){
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
    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        items.add(Constant.RightContextMenu.CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }

}

package com.sgswit.fx.controller.iCloud;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.sgswit.fx.utils.ICloudUtil.checkCloudAccount;

/**
 * <p>
 *  关闭家庭共享
 * </p>
 *
 * @author yanggang
 * @createTime 2023/12/11
 */
public class CloseFamilyController extends CustomTableView<Account> {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.CLOSE_FAMILY.getCode())));
        super.initialize(url, resourceBundle);
    }
    public void openImportAccountView(){
        openImportAccountView(List.of("account----pwd"));
    }

    @Override
    public void accountHandler(Account account){
        tableRefresh(account,"正在登录...");
        HttpResponse response;
        String clientId=DataUtil.getClientIdByAppleId(account.getAccount());
        if(!StringUtils.isEmpty(account.getStep()) && account.getStep().equals("00")){
            response= ICloudUtil.checkCloudAccount(clientId,account.getAccount(),account.getPwd()+ account.getAuthCode());
        }else{
            response= ICloudUtil.checkCloudAccount(clientId,account.getAccount(),account.getPwd() );
        }
        if(response.getStatus()==200){
            try {
                String rb = response.charset("UTF-8").body();
                JSONObject rspJSON = PListUtil.parse(rb);
                if("0".equals(rspJSON.getStr("status"))){
                    String message="查询成功";
                    JSONObject delegates= rspJSON.getJSONObject("delegates");
                    JSON comAppleMobileme = JSONUtil.parse(delegates.get("com.apple.mobileme"));
                    String status= comAppleMobileme.getByPath("status",String.class);
                    if("0".equals(status)){
                        tableRefresh(account,"登录成功，正在关闭...");
                        //关闭家庭共享
                        Map<String,Object> res=ICloudUtil.leaveFamily(ICloudUtil.getAuthByHttResponse(response),account.getAccount());
                        if(Constant.SUCCESS.equals(res.get("code"))){
                            message = "关闭成功";
                        }else {
                            message = res.get("msg").toString();
                        }
                    }else{
                        if(Constant.ACCOUNT_INVALID_HSA_TOKEN.equals(comAppleMobileme.getByPath("status-error",String.class))){
                            message=comAppleMobileme.getByPath("status-message",String.class);
                            account.getAuthData().put("code",Constant.TWO_FACTOR_AUTHENTICATION);
                        }else{
                            message="未激活iCloud账户";
                        }
                    }
                    tableRefresh(account,message);
                }else{
                    String message="";
                    for (Map.Entry<String, String> entry : Constant.errorMap.entrySet()) {
                        if (StringUtils.containsIgnoreCase(rspJSON.getStr("status-message"),entry.getKey())){
                            message=entry.getValue();
                            break;
                        }
                    }
                    if(!StringUtils.isEmpty(message)){
                        tableRefresh(account,message);
                    }else{
                        tableRefresh(account,rspJSON.getStr("status-message"));
                    }
                }

            }catch (Exception e){
                tableRefresh(account,"Apple ID或密码错误。");
                e.printStackTrace();
            }
        }else {
            tableRefresh(account,response.body());
        }

    }

    private void tableRefresh(Account account,String message){
        account.setNote(message);
        accountTableView.refresh();
        insertLocalHistory(List.of(account));
    }
    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }

    /**重新执行**/
    @Override
    protected void reExecute(Account account){
        new Thread(()->{
            accountHandler(account);
        }).start();
    }
    @Override
    protected void twoFactorCodeExecute(Account account, String authCode){
        try{
            Map<String,Object> res=account.getAuthData();
            if(Constant.TWO_FACTOR_AUTHENTICATION.equals(MapUtils.getStr(res,"code"))){
                account.setAuthCode(authCode);
                account.setStep("00");
                accountHandler(account);
            }else{
                alert("未下发双重验证码");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}

package com.sgswit.fx.controller.iCloud;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.ICloudUtil;
import com.sgswit.fx.utils.PListUtil;
import com.sgswit.fx.utils.PointUtil;
import javafx.fxml.FXML;
import javafx.scene.input.ContextMenuEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author DeZh
 * @title: CheckWhetherIcloudController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/2714:40
 */
public class CheckWhetherIcloudController extends CustomTableView<Account>{

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.CHECK_WHETHER_ICLOUD.getCode())));
        super.initialize(url, resourceBundle);
    }
    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        super.openImportAccountView(List.of("account----pwd"));
    }

    protected void checkCloudAcc(Account account) {
        setAndRefreshNote(account,"正在登录...");
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
                    JSON comAppleMobileme =JSONUtil.parse(delegates.get("com.apple.mobileme"));
                    String status= comAppleMobileme.getByPath("status",String.class);
                    if("0".equals(status)){
                        account.setSupport("支持");
                        account.setDsid(rspJSON.getStr("dsid"));
                    }else{
                        if(Constant.ACCOUNT_INVALID_HSA_TOKEN.equals(comAppleMobileme.getByPath("status-error",String.class))){
                            message=comAppleMobileme.getByPath("status-message",String.class);
                            account.getAuthData().put("code",Constant.TWO_FACTOR_AUTHENTICATION);
                        }else{
                            account.setSupport("不支持");
                        }
                    }
                    JSONObject ids= delegates.getJSONObject("com.apple.private.ids");
                    if("0".equals(ids.getStr("status"))){
                        String regionId=JSONUtil.parse(ids.get("service-data")).getByPath("invitation-context.region-id",String.class);
                        regionId= DataUtil.getNameByCountryCode(regionId.split(":")[1]);
                        account.setArea(regionId);
                        account.setDsid(rspJSON.getStr("dsid"));
                    }
                    account.setDataStatus("1");
                    tableRefreshAndInsertLocal(account,message);
                }else{
                    account.setDataStatus("0");
                    String message="";
                    for (Map.Entry<String, String> entry : Constant.errorMap.entrySet()) {
                        if (StringUtils.containsIgnoreCase(rspJSON.getStr("status-message"),entry.getKey())){
                            message=entry.getValue();
                            break;
                        }
                    }
                    if(!StringUtils.isEmpty(message)){
                        tableRefreshAndInsertLocal(account,message);
                    }else{
                        tableRefreshAndInsertLocal(account,rspJSON.getStr("status-message"));
                    }
                }
                account.setHasFinished(true);
            }catch (Exception e){
                account.setHasFinished(true);
                account.setDataStatus("0");
                tableRefreshAndInsertLocal(account,"Apple ID或密码错误。");
            }
        }else {
            account.setHasFinished(true);
            setAndRefreshNote(account,response.body());
        }
    }

    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }
    @Override
    public void accountHandler(Account account){
        checkCloudAcc(account);
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

package com.sgswit.fx.controller.iCloud;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ICloudView;
import com.sgswit.fx.model.ICloudFunctionalTesting;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.ICloudUtil;
import com.sgswit.fx.utils.PListUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class ICloudFunctionalTestingController extends ICloudView<ICloudFunctionalTesting> {

    /**
     * 导入账号
     */
    public void importAccountButtonAction(){
        openImportAccountView(List.of("account----pwd"));
    }

    @Override
    public void accountHandler(ICloudFunctionalTesting iCloudFunctionalTesting) {
        checkICloudStatus(iCloudFunctionalTesting);

        HttpResponse authRsp = iCloudLogin(iCloudFunctionalTesting);
        boolean verify = iCloudLoginVerify(authRsp, iCloudFunctionalTesting);
        if (!verify){
            return;
        }

        JSONObject jo = JSONUtil.parseObj(authRsp.body());
        String icloudMail = jo.getByPath("dsInfo.iCloudAppleIdAlias").toString();
        if(StrUtil.isNotEmpty(icloudMail)){
            iCloudFunctionalTesting.setIcloudMail(icloudMail);
        }

        String areaCode = jo.getByPath("dsInfo.countryCode",String.class);
        if (!StrUtil.isEmpty(areaCode)){
            iCloudFunctionalTesting.setArea(DataUtil.getNameByCountryCode(areaCode));
        }

        JSONObject webservices = (JSONObject) jo.getByPath("webservices");
        String mailStatus = "未激活";
        if(webservices.containsKey("mail")){
            String mailStatus1 = webservices.getByPath("mail.status").toString();
            if("active".equals(mailStatus1)){
                mailStatus = "已激活";
            }
        }
        iCloudFunctionalTesting.setMailStatus(mailStatus);
        setAndRefreshNote(iCloudFunctionalTesting,"查询成功");
    }

    public void checkICloudStatus(ICloudFunctionalTesting account){
        HttpResponse checkCloudAccountRsp= ICloudUtil.checkCloudAccount(DataUtil.getClientIdByAppleId(account.getAccount()),account.getAccount(),account.getPwd() );
        String body = checkCloudAccountRsp.charset("UTF-8").body();
        if(checkCloudAccountRsp.getStatus()==200){
            try {
                JSONObject rspJSON = PListUtil.parse(body);
                if("0".equals(rspJSON.getStr("status"))){
                    String message="查询成功";
                    JSONObject delegates= rspJSON.getJSONObject("delegates");
                    JSON comAppleMobileme =JSONUtil.parse(delegates.get("com.apple.mobileme"));
                    String status= comAppleMobileme.getByPath("status",String.class);
                    if("0".equals(status)){
                        account.setIsIcloudAccount("是");
                    }else{
                        if(Constant.ACCOUNT_INVALID_HSA_TOKEN.equals(comAppleMobileme.getByPath("status-error",String.class))){
                            message=comAppleMobileme.getByPath("status-message",String.class);
                        }else{
                            account.setIsIcloudAccount("否");
                        }
                    }
                    JSONObject ids= delegates.getJSONObject("com.apple.private.ids");
                    if("0".equals(ids.getStr("status"))){
                        String regionId=JSONUtil.parse(ids.get("service-data")).getByPath("invitation-context.region-id",String.class);
                        regionId= DataUtil.getNameByCountryCode(regionId.split(":")[1]);
                        account.setArea(regionId);
                    }
                    setAndRefreshNote(account,message);
                }else{
                    String message="";
                    for (Map.Entry<String, String> entry : Constant.errorMap.entrySet()) {
                        if (StringUtils.containsIgnoreCase(rspJSON.getStr("status-message"),entry.getKey())){
                            message=entry.getValue();
                            break;
                        }
                    }
                    if(!StringUtils.isEmpty(message)){
                        setAndRefreshNote(account,message);
                    }else{
                        setAndRefreshNote(account,rspJSON.getStr("status-message"));
                    }
                }
            }catch (Exception e){
                setAndRefreshNote(account,"Apple ID或密码错误。");
                e.printStackTrace();
            }
        }else {
            setAndRefreshNote(account,body);
        }
    }

}

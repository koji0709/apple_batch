package com.sgswit.fx.controller.query;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.PointUtil;
import javafx.fxml.FXML;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.ArrayList;
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
    public void onAccountInputBtnClick(){
        openImportAccountView(List.of("account----pwd"));
    }


    @Override
    public void accountHandler(Account account) {
        account.setHasFinished(false);
        //step1 sign 登录
        setAndRefreshNote(account,"登录中...");
        ThreadUtil.sleep(2000);
        HttpResponse step1Res = AppleIDUtil.signin(account);

        if(step1Res.getStatus()==503){
            account.setFailCount(account.getFailCount()+1);
            if(account.getFailCount() >= 5){
                account.setHasFinished(true);
                throw new ServiceException("操作频繁，请稍后重试！！");
            }
            ThreadUtil.sleep(10*1000);
            accountHandler(account);
        }else{
            if (step1Res.getStatus() != 409) {
                account.setHasFinished(true);
                queryFail(account,step1Res.body());

            }

            String step1Body = step1Res.body();
            JSON json = JSONUtil.parse(step1Body);

            ThreadUtil.sleep(2000);
            //step2 获取认证信息 -- 需要输入密保
            HttpResponse step21Res = AppleIDUtil.auth(account,step1Res);
            String authType = (String) json.getByPath("authType");
            if ("sa".equals(authType)) {
                setAndRefreshNote(account,"正常账号");
                insertLocalHistory(List.of(account));
            } else if ("hsa2".equals(authType)) {
                setAndRefreshNote(account,"此账号已开启双重认证");
                insertLocalHistory(List.of(account));
            }
        }
        account.setHasFinished(true);
    }

    private void queryFail(Account account,Object body) {
        System.out.println(body);
        JSONArray serviceErrors = JSONUtil.parseArray(JSONUtil.parseObj(body.toString()).get("serviceErrors").toString());
        String message = JSONUtil.parseObj(serviceErrors.get(0)).get("message").toString();
        if(message.contains("锁定")){
            account.setNote("账号已锁定");
        }else
        if(message.contains("密码")){
            account.setNote("Apple ID 或密码不正确");
        }else{
            account.setNote("Apple ID 未激活");
        }
        setAndRefreshNote(account,account.getNote());
        insertLocalHistory(List.of(account));
    }

}

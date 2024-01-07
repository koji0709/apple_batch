package com.sgswit.fx.controller.query;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.Problem;
import com.sgswit.fx.model.Question;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Paint;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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

    private Integer num = 0;

    public List<String> menuItem =new ArrayList<>(){{
        add(Constant.RightContextMenu.DELETE.getCode());
        add(Constant.RightContextMenu.REEXECUTE.getCode());
        add(Constant.RightContextMenu.COPY.getCode());
    }};

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItem,new ArrayList<>());
    }

    @Override
    protected void reExecute(Account account) {
        accountHandler(account);
    }

    @FXML
    public void onAccountInputBtnClick(){
        openImportAccountView(List.of("account----pwd"));
    }


    @Override
    public void accountHandler(Account account) {
        //step1 sign 登录
        account.setNote("登录中");
        accountTableView.refresh();
        HttpResponse step1Res = AppleIDUtil.signin(account);

        if(step1Res.body().startsWith("<html>")){
            num++;
            if(num >= 5){
                account.setNote("操作频繁，请稍后重试！");
                accountTableView.refresh();
                insertLocalHistory(List.of(account));
                return;
            }
            accountHandler(account);

        }
        if (step1Res.getStatus() != 409) {
            queryFail(account,step1Res.body());
            return ;
        }

        String step1Body = step1Res.body();
        JSON json = JSONUtil.parse(step1Body);
        if (json == null) {
            queryFail(account,step1Res.body());
            return ;
        }


        //step2 获取认证信息 -- 需要输入密保
        HttpResponse step21Res = AppleIDUtil.auth(account,step1Res);
        String authType = (String) json.getByPath("authType");
        if ("sa".equals(authType)) {
            //非双重认证
//            String body = step21Res.body();
//            String questions = JSONUtil.parseObj(body).getJSONObject("securityQuestions").get("questions").toString();
//            List<Question> qs = JSONUtil.toList(questions, Question.class);

            account.setNote("正常账号");
            accountTableView.refresh();
            insertLocalHistory(List.of(account));
        } else if ("hsa2".equals(authType)) {
            account.setNote("此账号已开启双重认证");
            accountTableView.refresh();
            insertLocalHistory(List.of(account));
        }
    }

    private void queryFail(Account account,Object body) {
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
        accountTableView.refresh();
        insertLocalHistory(List.of(account));
    }

}

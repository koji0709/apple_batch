package com.sgswit.fx.controller.query;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.base.TableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.Problem;
import com.sgswit.fx.model.Question;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 查询密保问题
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class SecurityQuestionQueryController extends TableView<Problem> {

    @FXML
    public void onAccountInputBtnClick(){
        openImportAccountView(Problem.class,"account----pwd");
    }




    public void accountHandler(Problem problem) {
        //step1 sign 登录
        Account account = new Account();
        account.setAccount(problem.getAccount());
        account.setPwd(problem.getPwd());
        HttpResponse step1Res = AppleIDUtil.signin(account);

        if (step1Res.getStatus() != 409) {
            queryFail(problem);
            return;
        }
        String step1Body = step1Res.body();
        JSON json = JSONUtil.parse(step1Body);
        if (json == null) {
            queryFail(problem);
            return;
        }
        //step2 获取认证信息 -- 需要输入密保
        HttpResponse step21Res = AppleIDUtil.auth(step1Res);
        String authType = (String) json.getByPath("authType");
        if ("sa".equals(authType)) {
            //非双重认证
            String body = step21Res.body();
            String questions = JSONUtil.parseObj(body).getJSONObject("securityQuestions").get("questions").toString();
            List<Question> qs = JSONUtil.toList(questions, Question.class);
            problem.setProblem1(qs.get(0).getQuestion());
            problem.setProblem2(qs.get(1).getQuestion());
            problem.setNote("查询完毕");
            accountTableView.refresh();
            insertLocalHistory(List.of(problem));
        } else if ("hsa2".equals(authType)) {
            problem.setNote("此账号已开启双重认证");
            accountTableView.refresh();
            insertLocalHistory(List.of(problem));
        }
    }

    private void queryFail(Problem problem) {
        String note = "查询失败，请确认用户名密码是否正确";
        problem.setNote(note);
        accountTableView.refresh();
        insertLocalHistory(List.of(problem));
    }

}

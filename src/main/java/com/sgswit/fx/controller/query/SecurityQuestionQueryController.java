package com.sgswit.fx.controller.query;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.Problem;
import com.sgswit.fx.model.Question;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.PointUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Paint;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * <p>
 * 查询密保问题
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class SecurityQuestionQueryController extends CustomTableView<Problem> {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.SECURITY_QUESTION.getCode())));
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

    public void onAccountInputBtnClick(){
        openImportAccountView(List.of("account----pwd"));
    }


    @Override
    public void accountHandler(Problem problem) {
        problem.setHasFinished(false);
        //step1 sign 登录
        problem.setNote("登录中");
        accountTableView.refresh();
        Account account = new Account();
        account.setAccount(problem.getAccount());
        account.setPwd(problem.getPwd());
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HttpResponse step1Res = AppleIDUtil.signin(account);
        problem.setNote("查询密保问题中");
        accountTableView.refresh();
        if (step1Res.getStatus() == 503) {
            //返还点数
            PointUtil.pointCost(FunctionListEnum.SECURITY_QUESTION.getCode(),PointUtil.in,account.getAccount());
            account.setFailCount(account.getFailCount()+1);
            if(account.getFailCount() >= 3){
                problem.setHasFinished(true);
                throw new ServiceException("操作频繁，请稍后重试！");
            }
            try {
                Thread.sleep(20*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            accountHandler(problem);
        }else{
            if (step1Res.getStatus() != 409) {
                problem.setHasFinished(true);
                throw new ServiceException("查询失败，请确认用户名密码是否正确");
            }
            String step1Body = step1Res.body();
            JSON json = JSONUtil.parse(step1Body);
            //step2 获取认证信息 -- 需要输入密保
            ThreadUtil.sleep(1500);
            HttpResponse step21Res = AppleIDUtil.auth(account,step1Res);
            String authType = (String) json.getByPath("authType");
            if ("sa".equals(authType)) {
                //非双重认证
                String body = step21Res.body();
                Document prodDoc = Jsoup.parse(body);
                Elements initDataElement = prodDoc.select("script[class=boot_args]");
                JSONObject object = JSONUtil.parseObj(initDataElement.html());
                String questions = object.getJSONObject("direct").getJSONObject("twoSV").getJSONObject("securityQuestions").get("questions").toString();
                List<Question> qs = JSONUtil.toList(questions, Question.class);
                problem.setProblem1(qs.get(0).getQuestion());
                problem.setProblem2(qs.get(1).getQuestion());
                problem.setNote("查询完毕");
                accountTableView.refresh();
                insertLocalHistory(List.of(problem));
            } else if ("hsa2".equals(authType)) {
                throw new ServiceException("此账号已开启双重认证");
            }
        }
        problem.setHasFinished(true);
    }


    private void queryFail(Problem problem) {
        String note = "查询失败，请确认用户名密码是否正确";
        problem.setNote(note);
        accountTableView.refresh();
        insertLocalHistory(List.of(problem));
    }
    private void queryFail(Problem problem,String notes) {
        problem.setNote(notes);
        accountTableView.refresh();
        insertLocalHistory(List.of(problem));
    }

}

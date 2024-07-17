package com.sgswit.fx.controller.query;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
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
import javafx.event.ActionEvent;
import javafx.scene.input.ContextMenuEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    public void onAccountInputBtnClick(ActionEvent actionEvent){
        openImportAccountView(List.of("account----pwd"),actionEvent);
    }


    @Override
    public void accountHandler(Problem problem) {
        problem.setHasFinished(false);
        //step1 sign 登录
        setAndRefreshNote(problem,"登录中...");
        Account account = new Account();
        account.setAccount(problem.getAccount());
        account.setPwd(problem.getPwd());
        ThreadUtil.sleep(1500);
        HttpResponse step1Res = AppleIDUtil.signin(account);
        setAndRefreshNote(problem,"查询密保问题中...");
        if (step1Res.getStatus() != 409) {
            String message="Apple ID或密码不正确";
            JSONArray errorArr = JSONUtil.parseObj(step1Res.body())
                    .getByPath("serviceErrors",JSONArray.class);
            if (errorArr != null && errorArr.size()>0){
                JSONObject err = (JSONObject)(errorArr.get(0));
                if ("-20209".equals(err.getStr("code"))){
                    message="此账号已被锁定";
                }
            }
            problem.setHasFinished(true);
            throw new ServiceException(message);
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
        problem.setHasFinished(true);
    }
}

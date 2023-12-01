package com.sgswit.fx.controller.query;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.base.CommonView;
import com.sgswit.fx.controller.base.TableView;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.OcrUtil;
import com.sgswit.fx.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 检测是否AppleID
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class WhetherAppleIdController extends TableView<Account> {

    @FXML
    public Button birthdayCountryQueryBtn;

    @FXML
    public void openImportAccountView(){
        openImportAccountView("account");
    }

    @Override
    public void accountHandler(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
        HttpResponse execute = HttpUtil.createGet(url)
                .header(headers)
                .execute();
        String body = execute.body();
        if(StrUtil.isEmpty(body)){
            account.setStatus("操作频繁！");
            account.setNote("查询失败");
            accountTableView.refresh();
            insertLocalHistory(List.of(account));
            return;
        }
        JSONObject object = JSONUtil.parseObj(body);
        String capId = object.get("id").toString();
        String capToken = object.get("token").toString();

        //解析图片
        JSONObject object1 = JSONUtil.parseObj(JSONUtil.parseObj(body).get("payload").toString());
        Object content = object1.get("content");
        String predict = OcrUtil.recognize(content.toString());
            String bodys = "{\"id\":\"" + account.getAccount() + "\",\"captcha\":{\"id\":" + capId + ",\"answer\":\"" + predict + "\",\"token\":\"" + capToken + "\"}}\n";
            HttpResponse execute1 = HttpUtil.createPost("https://iforgot.apple.com/password/verify/appleid")
                    .body(bodys)
                    .header(headers)
                    .execute();
            String body1 = execute1.body();
            if(body1.startsWith("<html>")){
                account.setStatus("网页503");
                account.setNote("查询失败");
                accountTableView.refresh();
                insertLocalHistory(List.of(account));
                return;
            }
            if (!StringUtils.isEmpty(execute1.body())) {
                JSONObject object2 = JSONUtil.parseObj(execute1.body());
                String service_errors = object2.getStr("service_errors");
                if (service_errors != null) {
                    JSONArray service_errors1 = JSONUtil.parseArray(service_errors);
                    String message = JSONUtil.parseObj(service_errors1.get(0)).getStr("message");
                    if(message.startsWith("请输入你")){
                       accountHandler(account);
                    }else {
                        account.setStatus(message);
                        account.setNote("查询成功");
                        accountTableView.refresh();
                        insertLocalHistory(List.of(account));
                    }

                } else if (object2.getStr("serviceErrors") != null) {
                    account.setStatus("此AppleID无效或不受支持");
                    account.setNote("查询成功");
                    accountTableView.refresh();
                    insertLocalHistory(List.of(account));
                }


            } else {
                HttpResponse location = HttpUtil.createGet("https://iforgot.apple.com" + execute1.header("Location"))
                        .header(headers)
                        .execute();

                if (StringUtils.isEmpty(location.body())) {
                    account.setStatus("此AppleID已开启双重认证");
                    account.setNote("查询成功");
                    accountTableView.refresh();
                    insertLocalHistory(List.of(account));
                } else if (JSONUtil.parseObj(location.body()).get("account") != null) {
                    account.setStatus("此AppleID已被锁定");
                    account.setNote("查询成功");
                    accountTableView.refresh();
                    insertLocalHistory(List.of(account));
                } else {
                    account.setStatus("此AppleID正常");
                    account.setNote("查询成功");
                    accountTableView.refresh();
                    insertLocalHistory(List.of(account));
                }
            }

    }

}

package com.sgswit.fx.controller.query;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.OcrUtil;
import javafx.scene.input.ContextMenuEvent;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;

/**
 * <p>
 * 检测是否AppleID
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class WhetherAppleIdController extends CustomTableView<Account> {

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
    public void openImportAccountView(){
        openImportAccountView(List.of("account"));
    }

    @Override
    public void accountHandler(Account account) {
        account.setNote("查询中");
        accountTableView.refresh();
        ThreadUtil.sleep(1000);
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
        String capId = object.getStr("id");
        String capToken = object.getStr("token");

        //解析图片
        ThreadUtil.sleep(1000);
        JSONObject payloadJson = JSONUtil.parseObj(JSONUtil.parseObj(body).getStr("payload"));
        String content = payloadJson.getStr("content");
        String predict = OcrUtil.recognize(content);
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
                }else if(JSONUtil.parseObj(location.body()).get("trustedPhones") != null){
                    account.setStatus("此AppleID已开启双重认证");
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

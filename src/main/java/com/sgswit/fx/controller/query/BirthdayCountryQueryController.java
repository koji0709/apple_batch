package com.sgswit.fx.controller.query;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.common.AppleIdView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;

import java.util.List;

/**
 * <p>
 * 查询生日国家
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class BirthdayCountryQueryController extends AppleIdView {


    public void onAccountInputBtnClick(){
        openImportAccountView(List.of("account----pwd-answer1-answer2-answer3"));
    }

    @Override
    public void accountHandler(Account account) {
        HttpResponse tokenRsp = login(account);
        if(tokenRsp.getStatus() != 200){
            queryFail(account);
        }
        HttpResponse step4Res = AppleIDUtil.account(tokenRsp);
        String managerBody = step4Res.body();
        JSON manager = JSONUtil.parse(managerBody);

        String state = (String) manager.getByPath("account.person.primaryAddress.countryCode");
        String area = (String) manager.getByPath("account.person.primaryAddress.countryName");
        String name = (String) manager.getByPath("name.fullName");
        String birthday = (String) manager.getByPath("localizedBirthday");
        String status = "正常";
        String note = "查询成功";

        account.setStatus(status);
        account.setState(state);
        account.setName(name);
        account.setBirthday(birthday);
        account.setNote(note);
        account.setArea(area);
        account.setLogtime(DateUtil.format(DateUtil.date(),"yyyy-MM-dd HH:mm:ss"));
        accountTableView.refresh();
        insertLocalHistory(List.of(account));
    }

    private void queryFail(Account account) {
        String note = "查询失败，请确认用户名密码是否正确";
        account.setNote(note);
        accountTableView.refresh();
    }

}

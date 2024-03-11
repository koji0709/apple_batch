package com.sgswit.fx.controller.query;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.AppleIdView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.PointUtil;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
/**
 * <p>
 * 查询生日国家
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class BirthdayCountryQueryController extends AppleIdView {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.BIRTHDAY_COUNTRY_QUERY.getCode())));
        super.initialize(url,resourceBundle);
        menuItem.add(Constant.RightContextMenu.WEB_TWO_FACTOR_CODE.getCode());
    }

    public void onAccountInputBtnClick(ActionEvent actionEvent){
        openImportAccountView(List.of("account----pwd","account----pwd-answer1-answer2-answer3"),actionEvent);
    }

    @Override
    public void accountHandler(Account account) {
        account.setHasFinished(false);
        setAndRefreshNote(account, "正在登录...");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 登录
        login(account);
        setAndRefreshNote(account,"正在读取用户信息...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            HttpResponse step4Res = AppleIDUtil.account(account);
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
            account.setHasFinished(true);
        }catch (Exception e){
            account.setHasFinished(true);
            throw new ServiceException("查询失败！");
        }
    }
}

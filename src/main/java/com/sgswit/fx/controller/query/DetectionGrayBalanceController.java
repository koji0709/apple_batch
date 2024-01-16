package com.sgswit.fx.controller.query;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.*;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.input.ContextMenuEvent;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;

/**
 * <p>
 *  检测灰余额
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class DetectionGrayBalanceController extends CustomTableView<Account> {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.CHECK_GRAY_BALANCE.getCode())));
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

    @Override
    protected void reExecute(Account account) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                accountHandler(account);
            }
        });
    }

    public void openImportAccountView(ActionEvent actionEvent) {
        openImportAccountView(List.of("account----pwd"));
    }

    @Override
    public void accountHandler(Account account) {
        try {
            //扣除点数
            Map<String,String> pointCost=PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.out,account.getAccount());
            if(!Constant.SUCCESS.equals(pointCost.get("code"))){
                alertUI(pointCost.get("msg"), Alert.AlertType.ERROR);
                return;
            }
            tableRefresh(account,"正在获取AppleID国家...");
            Map<String,Object> paras=new HashMap<>();
            paras.put("account",account.getAccount());
            paras.put("pwd",account.getPwd());
            paras.put("serviceKey", DataUtil.getWebClientIdByAppleId(account.getAccount()));
            HttpResponse response= WebLoginUtil.signin(paras);
            if(response.getStatus()==503){
                tableRefreshAndInsertLocal(account,"操作频繁，请稍后重试！");
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            } else if(response.getStatus()!=409){
               tableRefreshAndInsertLocal(account,WebLoginUtil.serviceErrorMessages(response.body()));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
               return;
            }
            String countryCode=MapUtils.getStr(paras,"countryCode");
            // 检测是否开启服务, 如果没有开启就直接到主页面方便测试
            String supportCountry = PropertiesUtil.getConfig("grayBalance.query.support.country");
            if(!StringUtils.containsIgnoreCase(supportCountry,countryCode)){
                tableRefreshAndInsertLocal(account,"不支持的国家");
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }
            if("USA".equalsIgnoreCase(countryCode)){
                paras.put("code2","");
            }else{
                String code2=DataUtil.getInfoByCountryCode(countryCode).getCode2();
                paras.put("code2",code2.toLowerCase());
            }
            account.setArea(DataUtil.getNameByCountryCode(countryCode));
            accountTableView.refresh();
            if("hsa2".equals(JSONUtil.parseObj(response.body()).getStr("authType"))){
                tableRefreshAndInsertLocal(account,"该账户为双重认证用户,请输入双重验证码");
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }


            paras.clear();
            paras.put("account",account.getAccount());
            paras.put("pwd",account.getPwd());
            paras.put("serviceKey", WebLoginUtil.createClientId());
            // 获取产品
            account.setNote("正在加载商品信息...");
            accountTableView.refresh();
            Thread.sleep(2000);
            Map<String,Object> prodMap = ShoppingUtil.getProd(paras);
            if(!Constant.SUCCESS.equals(prodMap.get("code"))){
                tableRefreshAndInsertLocal(account, MapUtil.getStr(prodMap,"msg"));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }
            account.setNote("商品信息加载成功...");
            accountTableView.refresh();
            Thread.sleep(2000);
            // 添加到购物车
            account.setNote("添加到购物车中...");
            accountTableView.refresh();
            Thread.sleep(2000);
            Map<String, Object> addMap = ShoppingUtil.add2bag(prodMap);
            if(!Constant.SUCCESS.equals(addMap.get("code"))){
                tableRefreshAndInsertLocal(account, MapUtil.getStr(addMap,"msg"));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }else{
                account.setNote("添加到购物车成功");
                accountTableView.refresh();
            }
            // 查看购物车
            account.setNote("获取购物车中商品信息");
            accountTableView.refresh();
            Thread.sleep(2000);
            Map<String,Object> bag = ShoppingUtil.shopbag(addMap);
            if(!Constant.SUCCESS.equals(bag.get("code"))){
                tableRefreshAndInsertLocal(account, MapUtil.getStr(bag,"msg"));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }
            // 提交购物车
            account.setNote("购物车中商品信息获取成功");
            accountTableView.refresh();
            Thread.sleep(2000);
            account.setNote("提交购物信息...");
            accountTableView.refresh();
            Map<String, Object> cheMap = ShoppingUtil.checkoutCart(bag);
            if(!Constant.SUCCESS.equals(cheMap.get("code"))){
                tableRefreshAndInsertLocal(account, MapUtil.getStr(cheMap,"msg"));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }
            //调登录页面
            account.setNote("登录页面加载中...");
            accountTableView.refresh();
            Thread.sleep(2000);
            Map<String,Object> signInMap = ShoppingUtil.shopSignIn(cheMap);
            if(!Constant.SUCCESS.equals(signInMap.get("code"))){
                tableRefreshAndInsertLocal(account, MapUtil.getStr(signInMap,"msg"));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }
            //登录
            account.setNote("登录中...");
            accountTableView.refresh();
            Thread.sleep(2000);
            HttpResponse signInResp = WebLoginUtil.signin(signInMap);
            if(signInResp.getStatus() == 409){
                if("hsa2".equals(JSONUtil.parseObj(signInResp.body()).getStr("authType"))){
                    tableRefreshAndInsertLocal(account,"该账户为双重认证用户,请输入双重验证码");
                    //返还点数
                    PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                    return;
                }

            }else if(signInResp.getStatus() == 200){

            }else if(signInResp.getStatus() == 503){
                tableRefreshAndInsertLocal(account,"操作频繁，请稍后重试！");
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }

            account.setNote("登录成功");
            accountTableView.refresh();
            Thread.sleep(2000);
            account.setNote("正在查询余额...");
            accountTableView.refresh();
            //回调applestore
            Map<String,Object> checkoutStartMap = ShoppingUtil.callBack(signInMap);
            if(!Constant.SUCCESS.equals(checkoutStartMap.get("code"))){
                tableRefreshAndInsertLocal(account, MapUtil.getStr(checkoutStartMap,"msg"));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }
            //chechout start
           checkoutStartMap = ShoppingUtil.checkoutStart(checkoutStartMap);
            //提交
            Map<String,Object> checkoutMap = ShoppingUtil.checkout(checkoutStartMap);
            if(!Constant.SUCCESS.equals(checkoutMap.get("code"))){
                tableRefreshAndInsertLocal(account, MapUtil.getStr(checkoutMap,"msg"));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }
            Thread.sleep(2000);
            //选择shipping - 邮寄
            Map<String,Object> shippingMap= ShoppingUtil.fillmentToShipping(checkoutMap);
            if(!Constant.SUCCESS.equals(shippingMap.get("code"))){
                tableRefreshAndInsertLocal(account, MapUtil.getStr(shippingMap,"msg"));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }
            Thread.sleep(2000);
            //填写shipping - 地址
            Map<String, Object> map = ShoppingUtil.shippingToBilling(shippingMap);
            if(!Constant.SUCCESS.equals(map.get("code"))){
                tableRefreshAndInsertLocal(account, MapUtil.getStr(map,"msg"));
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }
            account.setState(map.get("address").toString());
            accountTableView.refresh();
            //确认地址 - 显示账户余额
            Thread.sleep(2000);
            HttpResponse httpResponse = ShoppingUtil.selectedAddress(map);
            if(httpResponse.getStatus() != 200){
                tableRefreshAndInsertLocal(account,"余额查询失败");
                //返还点数
                PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
                return;
            }else {
                JSONObject meta = JSONUtil.parseObj(httpResponse.body());

                List<JSONObject> ja = (List<JSONObject>)meta.getByPath("body.checkout.billing.billingOptions.d.options");
                Integer num = 0;
                for(JSONObject o : ja){
                    if(o.containsKey("disabled")){
                        String disabled = o.get("disabled").toString();
                        if("true".equals(disabled)){
                            num ++;
                            break;
                        }
                    }
                }
                String balance = meta.getByPath("body.checkout.billing.billingOptions.selectedBillingOptions.appleBalance.appleBalanceInput.d.availableAppleBalance").toString();

                if(num == 0){
                    account.setStatus("启用");
                }else {
                    account.setStatus("禁用");
                }
                account.setBalance(balance);
                tableRefreshAndInsertLocal(account,"查询成功");
            }
        }catch (Exception e){
            //返还点数
            PointUtil.pointCost(FunctionListEnum.CHECK_GRAY_BALANCE.getCode(),PointUtil.in,account.getAccount());
            tableRefreshAndInsertLocal(account,"余额查询失败");
        }

    }

    private void tableRefresh(Account account,String message){
        account.setNote(message);
        accountTableView.refresh();
    }
    private void tableRefreshAndInsertLocal(Account account,String message){
        account.setNote(message);
        accountTableView.refresh();
        super.insertLocalHistory(List.of(account));
    }


}

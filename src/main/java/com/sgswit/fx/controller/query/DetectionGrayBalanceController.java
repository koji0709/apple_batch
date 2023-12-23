package com.sgswit.fx.controller.query;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.ShoppingUtil;
import com.sgswit.fx.utils.WebloginUtil;
import javafx.event.ActionEvent;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  检测灰余额
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class DetectionGrayBalanceController extends CustomTableView<Account> {

    public void openImportAccountView(ActionEvent actionEvent) {
        openImportAccountView(List.of("account----pwd"));
    }

    @Override
    public void accountHandler(Account account) {
        try {
            // 获取产品
            account.setNote("获取商品中");
            accountTableView.refresh();
            Map<String,Object> prodMap = ShoppingUtil.getProd(account);
            if(!Constant.SUCCESS.equals(prodMap.get("code"))){
                tableRefresh(account, MapUtil.getStr(prodMap,"msg"));
                return;
            }
            // 添加到购物车
            account.setNote("添加到购物车中");
            accountTableView.refresh();
            Map<String, Object> addMap = ShoppingUtil.add2bag(prodMap,account);
            if(!Constant.SUCCESS.equals(addMap.get("code"))){
                tableRefresh(account, MapUtil.getStr(addMap,"msg"));
                return;
            }
            // 查看购物车
            Map<String,Map<String,Object>> bag = ShoppingUtil.shopbag(account);
            if(!Constant.SUCCESS.equals(bag.get("code").get("code"))){
                tableRefresh(account, MapUtil.getStr(bag.get("code"),"msg"));
                return;
            }
            // 提交购物车
            Map<String, Object> cheMap = ShoppingUtil.checkoutCart(bag,account);
            if(!Constant.SUCCESS.equals(cheMap.get("code"))){
                tableRefresh(account, MapUtil.getStr(cheMap,"msg"));
                return;
            }
            //调登录页面
            account.setNote("登录页面加载中");
            accountTableView.refresh();
            Map<String,String> signInMap = ShoppingUtil.shopSignIn(cheMap.get("url").toString(),account);
            if(!Constant.SUCCESS.equals(signInMap.get("code"))){
                tableRefresh(account, MapUtil.getStr(signInMap,"msg"));
                return;
            }
            //登录
            account.setNote("登录中");
            accountTableView.refresh();
            HttpResponse signInResp = WebloginUtil.signin(signInMap,account);
            if(signInResp.getStatus() != 200){
                tableRefresh(account,"账号或密码错误");
                return;
            }
            if(!"sa".equals(JSONUtil.parseObj(signInResp.body()).getStr("authType"))){
                tableRefresh(account,"该账户为双重认证用户");
            }
            account.setNote("登录成功");
            accountTableView.refresh();
            //回调applestore
            Map<String,String> checkoutStartMap = ShoppingUtil.callBack(signInMap,account);
            if(!Constant.SUCCESS.equals(checkoutStartMap.get("code"))){
                tableRefresh(account, MapUtil.getStr(checkoutStartMap,"msg"));
                return;
            }
            //chechout start
            String checkoutUrl = ShoppingUtil.checkoutStart(checkoutStartMap,account);

            //提交
            Map<String,String> checkoutMap = ShoppingUtil.checkout(checkoutUrl,account);
            if(!Constant.SUCCESS.equals(checkoutMap.get("code"))){
                tableRefresh(account, MapUtil.getStr(checkoutMap,"msg"));
                return;
            }
            //选择shipping - 邮寄
            account.setNote("正在查询余额");
            accountTableView.refresh();
            String fillment = ShoppingUtil.fillmentToShipping(checkoutMap,account);
            if(!Constant.SUCCESS.equals(fillment)){
                tableRefresh(account,"查询失败");
                return;
            }
            //填写shipping - 地址
            Map<String, String> map = ShoppingUtil.shippingToBilling(checkoutMap, account);
            if(!Constant.SUCCESS.equals(map.get("code"))){
                tableRefresh(account, MapUtil.getStr(map,"msg"));
                return;
            }
            account.setState(map.get("address"));
            accountTableView.refresh();
            //确认地址 - 显示账户余额
            HttpResponse httpResponse = ShoppingUtil.selectedAddress(checkoutMap,account);
            if(httpResponse.getStatus() != 200){
                tableRefresh(account,"查询失败");
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
                tableRefresh(account,"查询成功");
            }
        }catch (Exception e){
            tableRefresh(account,"查询失败");
        }

    }

    private void tableRefresh(Account account,String message){
        account.setNote(message);
        accountTableView.refresh();
        insertLocalHistory(List.of(account));
    }


}

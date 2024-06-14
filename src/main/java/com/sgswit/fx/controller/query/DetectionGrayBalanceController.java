package com.sgswit.fx.controller.query;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.common.UnavailableException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.*;
import javafx.event.ActionEvent;
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

    public void openImportAccountView(ActionEvent actionEvent) {
        openImportAccountView(List.of("account----pwd"),actionEvent);
    }

    @Override
    public void accountHandler(Account account) {
        try {
            account.setHasFinished(false);
            setAndRefreshNote(account,"正在获取AppleID国家...");
            Map<String,Object> paras=new HashMap<>();
            paras.put("account",account.getAccount());
            paras.put("pwd",account.getPwd());
            paras.put("serviceKey", "a797929d224abb1cc663bb187bbcd02f7172ca3a84df470380522a7c6092118b");
            HttpResponse response= WebLoginUtil.signin(paras);
            if(response.getStatus()==503){
                account.setHasFinished(true);
                throw new UnavailableException();
            } else if(response.getStatus() == 409){
                if("hsa2".equals(JSONUtil.parseObj(response.body()).getStr("authType"))){
                    throw new ServiceException("暂不双重认证用户查询余额");
                }

            }else if(response.getStatus()!=200){
                account.setHasFinished(true);
                throw new ServiceException(AppleIDUtil.getValidationErrors(response,"登录失败"));
            }
            String countryCode=MapUtil.getStr(paras,"countryCode");
            account.setState(DataUtil.getNameByCountryCode(countryCode));
            // 检测是否开启服务, 如果没有开启就直接到主页面方便测试
            String supportCountry = PropertiesUtil.getConfig("grayBalance.query.support.country");
            if(!StringUtils.containsIgnoreCase(supportCountry,countryCode)){
                account.setHasFinished(true);
                throw new ServiceException("不支持的国家");
            }
            if("USA".equalsIgnoreCase(countryCode)){
                paras.put("code2","");
            }else{
                String code2=DataUtil.getInfoByCountryCode(countryCode).getCode2();
                paras.put("code2",code2.toLowerCase());
            }
            accountTableView.refresh();
            paras.clear();
            paras.put("account",account.getAccount());
            paras.put("pwd",account.getPwd());
            paras.put("serviceKey", WebLoginUtil.createClientId());
            // 获取产品
            setAndRefreshNote(account,"正在加载商品信息...");
            Thread.sleep(1000);
            Map<String,Object> prodMap = ShoppingUtil.getProd(paras);
            if(!Constant.SUCCESS.equals(prodMap.get("code"))){
                throw new ServiceException(MapUtil.getStr(prodMap,"msg"));
            }
            setAndRefreshNote(account,"商品信息加载成功...");
            Thread.sleep(1000);
            // 添加到购物车
            setAndRefreshNote(account,"添加到购物车中...");
            Thread.sleep(1000);
            Map<String, Object> addMap = ShoppingUtil.add2bag(prodMap);
            if(!Constant.SUCCESS.equals(addMap.get("code"))){
                if(addMap.get("code").equals("302")){
                    Thread.sleep(3000);
                    addMap = ShoppingUtil.add2bag(prodMap);
                    if(!Constant.SUCCESS.equals(addMap.get("code"))){
                        throw new ServiceException(MapUtil.getStr(addMap,"msg"));
                    }
                }else{
                    throw new ServiceException(MapUtil.getStr(addMap,"msg"));
                }
            }else{
                setAndRefreshNote(account,"添加到购物车成功");
            }
            // 查看购物车
            setAndRefreshNote(account,"获取购物车中商品信息");
            Thread.sleep(1000);
            Map<String,Object> bag = ShoppingUtil.shopbag(addMap);
            if(!Constant.SUCCESS.equals(bag.get("code"))){
                throw new ServiceException(MapUtil.getStr(bag,"msg"));
            }
            // 提交购物车
            setAndRefreshNote(account,"购物车中商品信息获取成功");
            Thread.sleep(1000);
            setAndRefreshNote(account,"提交购物信息...");
            Map<String, Object> cheMap = ShoppingUtil.checkoutCart(bag);
            if(!Constant.SUCCESS.equals(cheMap.get("code"))){
                throw new ServiceException(MapUtil.getStr(cheMap,"msg"));
            }
            //调登录页面
            setAndRefreshNote(account,"登录页面加载中...");
            Thread.sleep(1000);
            Map<String,Object> signInMap = ShoppingUtil.shopSignIn(cheMap);
            if(!Constant.SUCCESS.equals(signInMap.get("code"))){
                throw new ServiceException(MapUtil.getStr(signInMap,"msg"));
            }
            //登录
            setAndRefreshNote(account,"页面加载成功，登录中...");
            Thread.sleep(1000);
            HttpResponse signInResp = WebLoginUtil.signin(signInMap);
            if(signInResp.getStatus() == 200){

            }else if(signInResp.getStatus() == 503){
                throw new UnavailableException();
            }

            setAndRefreshNote(account,"登录成功...");

            Thread.sleep(1000);
            setAndRefreshNote(account,"正在查询余额...");
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
                throw new ServiceException( MapUtil.getStr(checkoutMap,"msg"));
            }
            Thread.sleep(1000);
            //选择shipping - 邮寄
            Map<String,Object> shippingMap= ShoppingUtil.fillmentToShipping(checkoutMap);
            if(!Constant.SUCCESS.equals(shippingMap.get("code"))){
                throw new ServiceException( MapUtil.getStr(shippingMap,"msg"));
            }
            Thread.sleep(1000);
            //填写shipping - 地址
            Map<String, Object> map = ShoppingUtil.shippingToBilling(shippingMap);
            if(!Constant.SUCCESS.equals(map.get("code"))){
                throw new ServiceException( MapUtil.getStr(map,"msg"));
            }
            account.setState(map.get("address").toString());
            //确认地址 - 显示账户余额
            Thread.sleep(1000);
            HttpResponse httpResponse = ShoppingUtil.selectedAddress(map);
            if(httpResponse.getStatus() != 200){
                throw new ServiceException("余额查询失败！");
            }else {
                JSONObject meta = JSONUtil.parseObj(httpResponse.body());
                List<JSONObject> ja = (List<JSONObject>)meta.getByPath("body.checkout.billing.billingOptions.d.options");
                if(null==ja){
                    throw new ServiceException("余额查询失败！");
                }
                boolean disabled = false;
                if(null!=ja &&ja.size()>0){
                    for(JSONObject o : ja){
                        if(o.getStr("moduleKey").equals("appleBalance")){
                            if("true".equals(o.get("disabled"))){
                                disabled=true;
                                break;
                            }
                        }
                    }
                }
                if(!disabled){
                    account.setStatus("正常");
                }else {
                    account.setStatus("禁用");
                }
                Object balance = meta.getByPath("body.checkout.billing.billingOptions.selectedBillingOptions.appleBalance.appleBalanceInput.d.availableAppleBalance");
                account.setBalance((null==balance||balance.equals(""))?"0":balance.toString());
                tableRefreshAndInsertLocal(account,"查询成功");
            }
        } catch (IORuntimeException e) {
            throw e;
        }catch (ServiceException ae){
            throw ae;
        }catch (Exception e){
            throw new ServiceException("余额查询失败，请稍后重试！");
        }

    }
}

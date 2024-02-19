package com.sgswit.fx.controller.base;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.HttpUtils;
import com.sgswit.fx.utils.PointUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * @author DELL
 */
public class SelfServiceChargeController extends CommonView implements Initializable {
    @FXML
    public Button cancelBtn;
    @FXML
    public Button confirmBtn;
    @FXML
    public TextField cardNoField;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void onCancelBtnClick(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    public void onConfirmBtnClick(ActionEvent actionEvent) {
        String cardNo="";
        if(null != cardNoField.getText().trim() && !"".equals(cardNoField.getText().trim())){
            cardNo=cardNoField.getText().trim();
        }
        try {
            if("".equals(cardNo)){
                alert("充值卡号不能为空", Alert.AlertType.ERROR);
            }else{
                if(cardNo.length()<10){
                    alert("充值卡号不正确!", Alert.AlertType.ERROR);
                }else{
                    //获取用户信息
                    Map<String, Object> userInfo = DataUtil.getUserInfo();
                    Map<String,String> map=new HashMap<>();
                    String userName= MapUtil.getStr(userInfo,"userName");
                    map.put("userName",userName);
                    map.put("cardNo",cardNo);
                    String body = JSONUtil.toJsonStr(map);
                    HttpResponse rsp = HttpUtils.post("/api/data/chargeToAccount",body);
                    JSON responseBody=JSONUtil.parse(rsp.body());
                    if (!responseBody.getByPath("code",String.class).equals(Constant.SUCCESS)){
                        String msg=responseBody.getByPath("msg",String.class);
                        alert(msg, Alert.AlertType.ERROR);
                        return;
                    }else{
                        alert("充值成功!", Alert.AlertType.INFORMATION);
                        String carNo=responseBody.getByPath("data.carNo",String.class);
                        String remainingPoints=responseBody.getByPath("data.remainingPoints",String.class);
                        DataUtil.setUserInfo("carNo",carNo);
                        DataUtil.setUserInfo("remainingPoints",remainingPoints);
                        //刷新点数
                        PointUtil.refreshRemainingPoints(remainingPoints);
                    }
                }
                Stage stage = (Stage) confirmBtn.getScene().getWindow();
                stage.close();

            }
        }catch (Exception e){
            alert("系统异常，请稍后重试!", Alert.AlertType.ERROR);
        }
    }
}

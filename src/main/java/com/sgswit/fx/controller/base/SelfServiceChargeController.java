package com.sgswit.fx.controller.base;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.HttpUtil;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.PropertiesUtil;
import com.sgswit.fx.utils.StageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * @author DELL
 */
public class SelfServiceChargeController implements Initializable {
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
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("充值提示");
                alert.setHeaderText("充值卡号不能为空！");
                alert.show();
            }else{
                if(cardNo.length()<10){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("充值提示");
                    alert.setHeaderText("充值卡号不正确！");
                    alert.show();
                }else{
                    //获取用户信息
                    String s = PropertiesUtil.getOtherConfig("login.info");
                    String decodeStr=Base64.decodeStr(s);
                    JSONObject json = JSONUtil.parseObj(decodeStr);
                    Map<String,String> map=new HashMap<>();
                    String userName=json.getByPath("userName",String.class);
                    map.put("userName",userName);
                    map.put("cardNo",cardNo);
                    String body = JSONUtil.toJsonStr(map);
                    HttpResponse rsp = HttpUtil.post("/api/data/chargeToAccount",body);
                    JSON responseBody=JSONUtil.parse(rsp.body());
                    if (!responseBody.getByPath("code",String.class).equals(Constant.SUCCESS)){
                        String msg=responseBody.getByPath("msg",String.class);
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("充值提示");
                        alert.setHeaderText(msg);
                        alert.show();
                        return;
                    }else{
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("充值提示");
                        alert.setHeaderText("充值成功！");
                        alert.show();
                        String carNo=responseBody.getByPath("data.carNo",String.class);
                        String remainingPoints=responseBody.getByPath("data.remainingPoints",String.class);
                        json.putByPath("carNo",carNo);
                        json.putByPath("remainingPoints",remainingPoints);
                        PropertiesUtil.setOtherConfig("login.info", Base64.encode(s));
                        //刷新点数
                        PointUtil.refreshRemainingPoints(remainingPoints);
                    }
                }
                Stage stage = (Stage) confirmBtn.getScene().getWindow();
                stage.close();

            }
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("充值提示");
            alert.setHeaderText("系统异常，请稍后重试！");
            alert.show();
        }
    }
}

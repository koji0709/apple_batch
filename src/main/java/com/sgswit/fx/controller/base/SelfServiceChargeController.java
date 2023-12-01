package com.sgswit.fx.controller.base;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.UserInfo;
import com.sgswit.fx.MainController;
import com.sgswit.fx.utils.HttpUtil;
import com.sgswit.fx.utils.PropertiesUtil;
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
public class SelfServiceChargeController implements Initializable {

    @FXML
    public Button cancelBtn;
    @FXML
    public Button confirmBtn;
    @FXML
    public TextField cardNoField;

    @FXML
    public MainController mainController;

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
                JSONObject object = JSONUtil.parseObj(Base64.decodeStr(s));
                Map<String,String> map=new HashMap<>();
                String userName=object.getByPath("userName").toString();
                map.put("userName",userName);
                map.put("cardNo",cardNo);
                String body = JSONUtil.toJsonStr(map);
                HttpResponse rsp = HttpUtil.post("/api/data/chargeToAccount",body);
                boolean success = HttpUtil.verifyRsp(rsp);
                if (!success){
                    String msg=JSONUtil.parse(rsp.body()).getByPath("msg").toString();
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

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HttpResponse rsp = HttpUtil.get("/userInfo/getInfoByUserName/"+userName);
                            boolean verify = HttpUtil.verifyRsp(rsp);
                            if (!verify){
                                return;
                            }
                            String userInfo=JSONUtil.parse(rsp.body()).getByPath("data").toString();
                            PropertiesUtil.setOtherConfig("login.info", Base64.encode(userInfo));
                            mainController.refreshUserInfo();
                        }
                    });




                }
            }
            Stage stage = (Stage) confirmBtn.getScene().getWindow();
            stage.close();
        }
    }
}

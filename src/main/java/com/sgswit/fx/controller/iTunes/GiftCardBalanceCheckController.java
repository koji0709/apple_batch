package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.GiftCard;
import com.sgswit.fx.utils.GiftCardUtil;
import com.sgswit.fx.utils.StageUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.awt.event.WindowListener;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author DeZh
 * @title: GiftCardBlanceCheckController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/2714:40
 */
public class GiftCardBalanceCheckController implements Initializable {

    @FXML
    public TableColumn seq;
    @FXML
    public TableColumn giftCardCode;
    @FXML
    public TableColumn balance;
    @FXML
    public TableColumn giftCardNumber;
    @FXML
    public TableColumn logTime;
    @FXML
    public TableColumn note;
    @FXML
    public ChoiceBox<Map<String,String>> countryBox;
    @FXML
    public TextField account_pwd;
    @FXML
    public Button accoutQueryBtn;
    @FXML
    public Label alertMessage;
    @FXML
    public Button loginBtn;

    @FXML
    private TableView accountTableView;

    private ObservableList<GiftCard> list = FXCollections.observableArrayList();

    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    private static Map<String,Object> hashMap=new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getCountry();
        alertMessage.setLabelFor(loginBtn);
        alertMessage.setText("等待初始化....");
    }
    private void getCountry(){
        String country = ResourceUtil.readUtf8Str("data/giftCard_query_support_country.json");
        for(Object o:JSONUtil.parseArray(country)){
            JSONObject jsonObject=(JSONObject)o;
            countryBox.getItems().add(new HashMap<>(){{
                put("name",jsonObject.getStr("name"));
                put("code",jsonObject.getStr("code"));
            }});
        }
        //默认美国
        countryBox.getSelectionModel().select(0);

        countryBox.converterProperty().set(new StringConverter<Map<String, String>>() {
            @Override
            public String toString(Map<String, String> map) {
                return map.get("name");
            }
            @Override
            public Map<String, String> fromString(String string) {
                return null;
            }
        });
        countryBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                loginAndInit(hashMap);
            }
        });
    }


    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/giftCard-input-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 300);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("礼品卡导入");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();

        GiftCardInputPopupController c = fxmlLoader.getController();
        if(null == c.getData() || "".equals(c.getData())){
            return;
        }
        String[] lineArray = c.getData().split("\n");
        for(String item : lineArray){
            GiftCard giftCard = new GiftCard();
            giftCard.setSeq(list.size()+1);
            giftCard.setGiftCardCode(item);
            list.add(giftCard);
        }
        initAccountTableView();
        accountTableView.setEditable(true);
        accountTableView.setItems(list);
    }
    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/giftCard-querylog-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 950, 600);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("账户查询记录");
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
    @FXML
    protected void onAccountExportBtnClick() throws Exception{
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("友情提示");
        alert.setHeaderText("功能建设中，敬请期待");
        alert.show();
    }

    @FXML
    protected void onAccountClearBtnClick() throws Exception{
        this.list.clear();
        accountTableView.refresh();
    }
    @FXML
    public void onClickLoginBtn(ActionEvent actionEvent) {
        loginAndInit(hashMap);
    }

    @FXML
    protected void onAccountQueryBtnClick() throws Exception{
        if(StringUtils.isEmpty(account_pwd.getText())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("信息");
            alert.setHeaderText("请输入一个AppleID作为初始化，账号格式为：账号----密码");
            alert.show();
            return;
        }
        Map<String,Object> res=new HashMap<>();
        if(list.size() < 1){
            return;
        }
        if(null==hashMap || hashMap.size()==0){
            loginAndInit(res);
            hashMap=res;
        }else{
            res=hashMap;
        }
        for(GiftCard giftCard:list){
//            checkBalance(giftCard, res);
            //判断是否已执行或执行中,避免重复执行
            if(!StrUtil.isEmptyIfStr(giftCard.getNote())){
                continue;
            }else{
                checkBalance(giftCard, res);
//                Map<String, Object> finalRes = res;
//                new Thread(new Runnable() {
//                    @Override
//                    public void run(){
//                        try {
//                            try {
//                                checkBalance(giftCard, finalRes);
//                            } catch (Exception e) {
//                                accoutQueryBtn.setDisable(false);
//                                accoutQueryBtn.setText("开始执行");
//                                accoutQueryBtn.setTextFill(Paint.valueOf("#238142"));
//                                e.printStackTrace();
//                            }
//                        }finally {
//                            //JavaFX Application Thread会逐个阻塞的执行这些任务
//                            Platform.runLater(new Task<Integer>() {
//                                @Override
//                                protected Integer call() {
//                                    accoutQueryBtn.setDisable(false);
//                                    accoutQueryBtn.setText("开始执行");
//                                    accoutQueryBtn.setTextFill(Paint.valueOf("#238142"));
//                                    return 1;
//                                }
//                            });
//                        }
//                    }
//                }).start();
            }
        }
    }
    /**
    　* 登录并初始化
      * @param
    　* @return cn.hutool.http.HttpResponse
    　* @throws
    　* @author DeZh
    　* @date 2023/11/1 11:15
    */
    protected void loginAndInit(Map<String,Object> paras){
        if(StringUtils.isEmpty(account_pwd.getText())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("信息");
            alert.setHeaderText("请输入一个AppleID作为初始化，账号格式为：账号----密码");
            alert.show();
            return;
        }
        String[] its =account_pwd.getText().split("----");
        String account=its[0];
        String pwd=its[1];
        String countryCode=countryBox.getSelectionModel().getSelectedItem().get("code");

        //https://secure.store.apple.com/shop/giftcard/balance
        HttpResponse pre1 = GiftCardUtil.shopPre1(countryCode);
        if(pre1.getStatus() != 303){
            alertMessage.setText("网络错误");
            alertMessage.setTextFill(Paint.valueOf("red"));;
            return ;
        }

        //https://secure4.store.apple.com/shop/giftcard/balance
        HttpResponse pre2 = GiftCardUtil.shopPre2(pre1);
        if(pre2.getStatus() != 302){
            alertMessage.setText("网络错误");
            alertMessage.setTextFill(Paint.valueOf("red"));;
            return ;
        }

        //https://secure4.store.apple.com/shop/signIn?ssi=1AAABiatkunsgRa-aWEWPTDH2TWsHul_CZ2TC62v9QxcThhc-EPUrFW8AAAA3aHR0cHM6Ly9zZWN1cmU0LnN0b3JlLmFwcGxlLmNvbS9zaG9wL2dpZnRjYXJkL2JhbGFuY2V8fAACAf0PkQUMMDk-ffBr4IVwBmhKDAsCeTbIe2k-7oOanvAP
        HttpResponse pre3 = GiftCardUtil.shopPre3(pre1,pre2);

        paras=GiftCardUtil.jXDocument(pre2, pre3,paras);

        HttpResponse step0Res = GiftCardUtil.federate(account,paras);
        String a= MapUtil.getStr(paras,"a");
        HttpResponse step1Res = GiftCardUtil.signinInit(account,a,step0Res,paras);

        HttpResponse step2Res = GiftCardUtil.signinCompete(account,pwd,paras,step1Res,pre1,pre3);
        if(null!=JSONUtil.parse(step2Res.body()).getByPath("serviceErrors")){
            alertMessage.setText(JSONUtil.parse(step2Res.body()).getByPath("serviceErrors.message").toString());
            alertMessage.setTextFill(Paint.valueOf("red"));
            return ;
        }
        //step3 shop signin
        HttpResponse step3Res= GiftCardUtil.shopSignin(step2Res,pre1,paras);
        alertMessage.setText("初始化成功，下次启动将自动执行初始化");
        alertMessage.setTextFill(Paint.valueOf("#238142"));

    }
    protected void checkBalance(GiftCard giftCard,Map<String,Object> paras) {
        Date nowDate=new Date();
        tableRefresh(giftCard,"正在登录...");
        HttpResponse step4Res = GiftCardUtil.checkBalance(paras,giftCard.getGiftCardCode());
        if(step4Res.getStatus()!=200){
            giftCard.setNote("网络错误");
            accountTableView.refresh();
        }else{
            JSON bodyJson= JSONUtil.parse(step4Res.body());
            String status=bodyJson.getByPath("head.status").toString();
            if(!"200".equals(status)){
                giftCard.setNote("网络错误");
                accountTableView.refresh();
                return;
            }
            Object balance=bodyJson.getByPath("body.giftCardBalanceCheck.d.balance");
            Object giftCardNumber=bodyJson.getByPath("body.giftCardBalanceCheck.d.giftCardNumber");
            if(null==balance){
                giftCard.setNote("这不是有效的礼品");
                accountTableView.refresh();
            }else{
                giftCard.setLogTime(sdf.format(nowDate));
                giftCard.setBalance(balance.toString());
                giftCard.setGiftCardNumber(giftCardNumber.toString().split(";")[1]);
                giftCard.setNote("查询成功");
                accountTableView.refresh();
            }
        }
    }

    private void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<GiftCard,Integer>("seq"));
        giftCardCode.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("giftCardCode"));
        balance.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("balance"));
        logTime.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("logTime"));
        giftCardNumber.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("giftCardNumber"));
        note.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("note"));
    }
    private void tableRefresh(GiftCard giftCard,String message){
        giftCard.setNote(message);
        accountTableView.refresh();
    }

    public void onStopBtnClick(ActionEvent actionEvent) {

    }
}

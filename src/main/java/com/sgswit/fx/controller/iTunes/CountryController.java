package com.sgswit.fx.controller.iTunes;

import com.sgswit.fx.model.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class CountryController {

    @FXML
    private TableView accountTableView;
    @FXML
    private TableColumn seqNo;
    @FXML
    private TableColumn account;
    @FXML
    private TableColumn pwd;
    @FXML
    private TableColumn targetCountry;
    @FXML
    private TableColumn note;

    @FXML
    private TableColumn answer1;
    @FXML
    private TableColumn answer2;
    @FXML
    private TableColumn answer3;


    private ObservableList<Account> list = FXCollections.observableArrayList();

    public CountryController(){


    }



//    @FXML
//    protected void onAccountInputBtnClick() throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("account-input-popup.fxml"));
//
//        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
//        scene.getRoot().setStyle("-fx-font-family: 'serif'");
//
//        Stage popupStage = new Stage();
//
//        popupStage.setTitle("账户导入");
//
//        popupStage.initModality(Modality.WINDOW_MODAL);
//        popupStage.setScene(scene);
//        popupStage.showAndWait();
//
//
//        AccountInputPopupController c = (AccountInputPopupController)fxmlLoader.getController();
//        if(null == c.getAccounts() || "".equals(c.getAccounts())){
//            return;
//        }
//
//        String[] lineArray = c.getAccounts().split("\n");
//
//        for(String item : lineArray){
//            String[] its = item.split("----");
//
//            Account account = new Account();
//            account.setSeq(list.size()+1);
//            account.setAccount(its[0]);
//
//            String[] pas = its[1].split("-");
//            if(pas.length == 4){
//                account.setPwd(pas[0]);
//                account.setAnswer1(pas[1]);
//                account.setAnswer2(pas[2]);
//                account.setAnswer3(pas[3]);
//            }else{
//                account.setPwd(its[1]);
//            }
//            list.add(account);
//        }
//        initAccoutTableView();
//        accountTableView.setItems(list);
//    }

    @FXML
    protected void onAccountClearBtnClick() throws Exception{
        this.list.clear();
        accountTableView.refresh();
    }


}

package com.sgswit.fx.controller.common;

import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
import com.sgswit.fx.enums.DataImportEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AccountImportUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author DELL
 */
public class CommDataInputPopupController<T> implements Initializable {
    @FXML
    public VBox notes;
    @FXML
    private TextArea accountTextArea;
    private List<String> description;
    @FXML
    private Button accountImportBtn;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Insets padding = new Insets(0, 0, 0, 20);
        int index=1;
        for (String s:description){
            Label label = new Label(index+"."+s);
            label.setPadding(padding);
            notes.setSpacing(5);
            notes.setPadding(new Insets(5, 5, 5, 5));
            notes.getChildren().add(label);
            index++;
        }
    }

    @FXML
    protected void onAccountImportBtnClick() throws Exception{
        // get a handle to the stage
        Stage stage = (Stage) accountImportBtn.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    public String getAccounts(){
        return this.accountTextArea.getText();
    }


    public void importData(ObservableList<T> list, DataImportEnum dataImportEnum) throws IOException {
        Insets padding = new Insets(0, 0, 0, 20);
           int index=1;
//        for (String s:description){
//            Label label = new Label(s);
//            label.setPadding(padding);
//            notes.setSpacing(5);
//            notes.setPadding(new Insets(5, 5, 5, 5));
//            notes.getChildren().add(label);
//            index++;
//        }
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/base/comm-data-input-popup.fxml"));
        description=dataImportEnum.getDescription();
        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();
        popupStage.setScene(scene);




        popupStage.setTitle("账户导入");

        popupStage.initModality(Modality.APPLICATION_MODAL);

        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);



        popupStage.showAndWait();
        Account account = new Account();
        account.setSeq(list.size()+1);
        account.setAccount("123");
        list.add((T)account);

//        CommDataInputPopupController c = fxmlLoader.getController();
//        if(null == c.getAccounts() || "".equals(c.getAccounts())){
//            return;
//        }
//        String[] lineArray = c.getAccounts().split("\n");
//        for(String item : lineArray){
//            String[] its = item.split("----");
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


    }
}

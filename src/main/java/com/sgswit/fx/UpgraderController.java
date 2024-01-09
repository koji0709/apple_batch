package com.sgswit.fx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.http.HttpUtil;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.StageUtil;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author DeZh
 * @title: UpgraderController
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/913:51
 */
public class UpgraderController implements Initializable,Serializable {
    @FXML
    public ProgressBar progressBar;
    @FXML
    public TextArea notes;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File fFile = new File("news.ini");
        if(!fFile.exists()){
            return;
        }
        String text = FileUtil.readString(fFile, Charset.defaultCharset());
        notes.setText(text);
        progressBar.progressProperty().bind(service.progressProperty());
        service.restart();
    }

    Service<Integer> service = new Service<>() {
        String filePath = System.getProperty("user.dir");
        String filename = filePath + "ceshi.exe";
        String url = "https://i-710.osslan.com:446/01091600157092607bb/2024/01/08/52069ff0c1bfc9c02810b9b259ec1414.exe?st=GwtQtQNDr3UVi1KnzU3UTQ&e=1704792767&b=AbsOsVfSWbpXnFKYAXEPJlYjXkMDclQiVmkMZAXkX9RVvlmwCI8DiVPjBPZXgwPgCcwI0gIjBlYEdws1UHlVYgF9DjhXeVk5V3pSYQ_c_c&fi=157092607&pid=223-91-60-76&up=2&mp=0&co=0";

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() {

                    HttpUtil.downloadFile(url, FileUtil.file(filename), new StreamProgress() {

                        @Override
                        public void start() {
                            System.out.println("开始下载。。。。");
                        }

                        @Override
                        public void progress(long totalSize, long progressSize) {
                            updateProgress(progressSize, totalSize);
                        }

                        @Override
                        public void finish() {
                            try {
                                Platform.runLater(()->{
                                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                                    confirm.setHeaderText("");
                                    confirm.setContentText("下载完成，是否立即运行程序");
                                    Optional<ButtonType> type = confirm.showAndWait();
                                    if (type.get()==ButtonType.OK){
                                        try {
                                            Runtime.getRuntime().exec("cmd /k start .\\update.vbs");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }else{

                                    }
                                    Stage upgraderStage= StageUtil.get(StageEnum.UPGRADER);
                                    upgraderStage.close();
                                    //关闭应用程序
                                    Platform.exit();
                                    System.exit(0);


                                });
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    return 1;
                }
            };
        }
    };

}

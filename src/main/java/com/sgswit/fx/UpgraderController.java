package com.sgswit.fx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import com.sgswit.fx.utils.PropertiesUtil;
import com.sgswit.fx.utils.SystemUtils;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author DeZh
 * @title: UpgraderController
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/913:51
 */
public class UpgraderController implements Initializable {
    @FXML
    public ProgressBar progressBar;
    @FXML
    public TextArea notes;

    public Map<String,Object> data;

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File fFile = new File("news.ini");
        if(!fFile.exists()){
            return;
        }
        String text = FileUtil.readString(fFile, Charset.defaultCharset());
        notes.setText(text);
        progressBar.progressProperty().bind(service.progressProperty());
    }





    public UpgraderController() {
    }
    /**
    　*启动下载
      * @param
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2024/1/10 17:40
    */
    public void startDownload() {
        service.restart();
    }




    Service<Integer> service = new Service<>() {
        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() {
                    String filePath = System.getProperty("user.dir");
                    String filename = filePath +"/"+ MapUtil.getStr(data,"name");
                    File file=new File(filename);
                    String url = MapUtil.getStr(data,"url");
                    HttpUtil.downloadFile(url, file, new StreamProgress() {
                        @Override
                        public void start() {}

                        @Override
                        public void progress(long totalSize, long progressSize) {
                            updateProgress(progressSize, totalSize);
                        }

                        @Override
                        public void finish() {
                            String clientPath=file.getPath();
                            try {
                                Platform.runLater(()->{
                                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                                    confirm.setHeaderText("");
                                    confirm.setContentText("下载完成，是否立即运行程序");
                                    Stage stage = (Stage) confirm.getDialogPane().getScene().getWindow();
                                    String logImg= PropertiesUtil.getConfig("softwareInfo.log.path");
                                    stage.getIcons().add(new Image(this.getClass().getResource(logImg).toString()));
                                    Optional<ButtonType> type = confirm.showAndWait();
                                    if (type.get()==ButtonType.OK){
                                        if(SystemUtils.isWindows()){
                                            try {
                                                delete(true,clientPath);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }else if (SystemUtils.isMacOs()){
                                            try {
                                                Desktop.getDesktop().open(new File(clientPath));
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }else{
                                        if(SystemUtils.isWindows()){
                                            try {
                                                delete(false,null);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
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
    private void delete(boolean isStart,String clientPath) throws IOException {
        //删除文件
        FileWriter fw= new FileWriter("delSelf.bat", Charset.forName("GBK"),true);
        try{
            String rootPath=System.getProperty("user.dir");
            File file=new File(rootPath);
            fw.write("@echo off");
            fw.write("\r\n");
            fw.write("%1 mshta vbscript:CreateObject(\"WScript.Shell\").Run(\"%~n0 ::\",0,FALSE)(window.close)&&exit");
            //杀死当前进程
            File[] files = new File(file.getAbsolutePath()).listFiles();
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".exe") && !f.getName().equalsIgnoreCase(MapUtil.getStr(data,"name"))) {
                    fw.write("\r\n");
                    fw.write("taskkill /f /im \""+f.getName()+"\"");
                    //文件
                    fw.write("\r\n");
                    fw.write("del \""+rootPath+"\\"+f.getName()+"\"");
                }
            }
            if (isStart){
                //开启新程序
                fw.write("\r\n");
                //杀死当前进程
                fw.write("start "+clientPath);
            }
            //删除bat文件
            fw.write("\r\n");
            fw.write("del %0");
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Runtime.getRuntime().exec("delSelf.bat");
        }
    }
}

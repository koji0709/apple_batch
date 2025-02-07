package com.sgswit.fx.c;

import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.utils.PropertiesUtil;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ProcessChecker  {

    private static class ResultVo{
        private boolean flag = false;
        private String msg;

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }


    //定时任务
    private static ScheduledExecutorService scheduledExecutorService;
    private static ScheduledFuture scheduledFuture;
    private static ResultVo isWindowsProcessRunning() {
        ResultVo resultVo=new ResultVo();
        try {
            // 使用 tasklist 命令查看运行的进程
            Process process = Runtime.getRuntime().exec("tasklist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (StrUtil.containsIgnoreCase(line,"Charles.exe")) {
                    resultVo.setFlag(true);
                    resultVo.setMsg("请关闭Charles后使用");
                    return resultVo;
                }else if (StrUtil.containsIgnoreCase(line,"Fiddler.exe")) {
                    resultVo.setFlag(true);
                    resultVo.setMsg("请关闭Fiddler后使用");
                    return resultVo;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultVo;
    }
    private static ResultVo isMacProcessRunning() {
        ResultVo resultVo=new ResultVo();
        try {
            // 使用 ps aux 命令获取所有进程
            Process process = Runtime.getRuntime().exec("ps aux");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            // 逐行读取进程信息
            while ((line = reader.readLine()) != null) {
                if (StrUtil.containsIgnoreCase(line,"Charles")) {
                    resultVo.setFlag(true);
                    resultVo.setMsg("请关闭Charles后使用");
                    return resultVo;
                }else if (StrUtil.containsIgnoreCase(line,"Fiddler")) {
                    resultVo.setFlag(true);
                    resultVo.setMsg("请关闭Fiddler后使用");
                    return resultVo;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultVo;
    }


    public static void timer(){
        //1-windows,2-mac
        String platform= PropertiesUtil.getConfig("softwareInfo.platform");
        boolean debug= PropertiesUtil.getConfigBool("debug",false);
        scheduledExecutorService= getScheduledExecutorService();
        scheduledFuture= scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {

                ResultVo vo=new ResultVo();
                if(platform.equals("1")){
                    vo=isWindowsProcessRunning();
                }else if(platform.equals("2")){
                    vo=isMacProcessRunning();
                }
                if(vo.flag && !debug){
                    Platform.exit();
                    System.exit(0);
                }
            }catch (Exception e){

            }
        }, 0, 3, TimeUnit.SECONDS);
    }
    /*
    　* 获取线程池
     * @param
    　* @return java.util.concurrent.ScheduledExecutorService
    　* @throws
    　* @author DeZh
    　* @date 2024/7/8 15:33
     */
    private static ScheduledExecutorService getScheduledExecutorService(){
        if(null==scheduledExecutorService || scheduledExecutorService.isShutdown()){
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        return scheduledExecutorService;
    }
}
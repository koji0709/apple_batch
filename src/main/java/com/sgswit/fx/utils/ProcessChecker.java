package com.sgswit.fx.utils;

import cn.hutool.core.util.StrUtil;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;

public class ProcessChecker {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledFuture;

    private static class ResultVo {
        private boolean flag;
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

    private ProcessChecker() {
        // 工具类禁止实例化
    }

    private static ResultVo checkWindowsProcess() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Runtime.getRuntime().exec("tasklist").getInputStream()))) {
            return checkProcess(reader);
        }
    }

    private static ResultVo checkMacProcess() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Runtime.getRuntime().exec("ps aux").getInputStream()))) {
            return checkProcess(reader);
        }
    }

    private static ResultVo checkProcess(BufferedReader reader) throws IOException {
        ResultVo resultVo = new ResultVo();
        String line;
        while ((line = reader.readLine()) != null) {
            if (StrUtil.containsIgnoreCase(line, "Charles")) {
                resultVo.setFlag(true);
                resultVo.setMsg("请关闭 Charles 后使用");
                return resultVo;
            } else if (StrUtil.containsIgnoreCase(line, "Fiddler")) {
                resultVo.setFlag(true);
                resultVo.setMsg("请关闭 Fiddler 后使用");
                return resultVo;
            }
        }
        return resultVo;
    }

    public static void startTimer() {
        String platform = PropertiesUtil.getConfig("softwareInfo.platform");
        boolean debugMode = PropertiesUtil.getConfigBool("debug", false);

        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }

        scheduledFuture = SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                ResultVo result;
                if ("1".equals(platform)) {
                    result = checkWindowsProcess();
                } else if ("2".equals(platform)) {
                    result = checkMacProcess();
                } else {
                    return; // 未知平台
                }

                if (result.isFlag() && !debugMode) {
                    System.out.println(result.getMsg());
                    Platform.exit();
                    System.exit(0);
                }
            } catch (IOException e) {
                e.printStackTrace(); // 也可以用日志系统统一记录
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    public static void shutdownTimer() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
        SCHEDULER.shutdownNow();
    }
}

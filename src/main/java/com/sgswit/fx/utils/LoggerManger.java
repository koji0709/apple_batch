package com.sgswit.fx.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * @author DeZh
 * @title: LoggerManger
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/3/1719:57
 */
public class LoggerManger {
    private static final Logger logger = Logger.getLogger(LoggerManger.class.getName());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static{
        try {
            // 获取用户主目录
            String userHome = System.getProperty("user.home");
            // 构建 .apple_batch 目录路径
            String logDir = userHome + "/.apple_batch";
            java.io.File dir = new java.io.File(logDir);
            // 如果目录不存在则创建
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 构建日志文件路径
            String logFilePath = logDir + "/application.log";
            FileHandler fileHandler = new FileHandler(logFilePath, true);
            Handler consoleHandler = new ConsoleHandler();
            DetailedFormatter formatter = new DetailedFormatter();
            fileHandler.setFormatter(formatter);
            consoleHandler.setFormatter(formatter);

            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
        } catch (SecurityException | IOException e) {
            logger.severe("无法配置日志记录器: " + e.getMessage());
        }
    }
    public static void info(String message,Throwable thrown){
        thrown.printStackTrace();
        logger.log(Level.INFO,message,thrown);
    }
    public static void info(String message){
        logger.log(Level.INFO,message);
    }

    static class DetailedFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%1$s - %2$s %n",
                    dateFormat.format(new Date(record.getMillis())),
                    formatMessage(record)));

            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw);
                } catch (Exception ex) {
                    // 异常处理
                }
            }
            return sb.toString();
        }
    }


}

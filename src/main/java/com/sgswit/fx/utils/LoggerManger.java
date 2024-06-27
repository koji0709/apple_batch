package com.sgswit.fx.utils;

import java.io.IOException;
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
            FileHandler fileHandler = new FileHandler("application.log", true);
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
        logger.log(Level.INFO,message,thrown);
    }
    public static void info(String message){
        logger.log(Level.INFO,message);
    }

    static class DetailedFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            // %1$s - %2$s.%3$s - %4$s %n -> 时间 - 类.方法 - 自定义
            return String.format("%1$s - %2$s %n",
                    dateFormat.format(new Date(record.getMillis())),
                    formatMessage(record));
        }
    }


}

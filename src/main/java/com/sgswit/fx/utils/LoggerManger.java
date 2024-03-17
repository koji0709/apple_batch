package com.sgswit.fx.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author DeZh
 * @title: LoggerManger
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/3/1719:57
 */
public class LoggerManger {
    private static final Logger LOGGER = Logger.getLogger(LoggerManger.class.getName());
    static{
        try {
            FileHandler fileHandler = new FileHandler("application.log", true);
            LOGGER.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (SecurityException | IOException e) {
            LOGGER.severe("无法配置日志记录器: " + e.getMessage());
        }
    }
    public static void info(String message,Throwable thrown){
        LOGGER.log(Level.INFO,message,thrown);
    }
    public static void info(String message){
        LOGGER.log(Level.INFO,message);
    }
}

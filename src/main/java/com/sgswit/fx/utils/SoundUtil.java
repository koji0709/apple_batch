package com.sgswit.fx.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.application.Platform;

import java.net.URL;

public class SoundUtil {
    private static MediaPlayer currentPlayer;
    private static String resourcePath="audio/alert.wav";
    public static void playSound() {
        playSound(resourcePath);
    }
    public static void playSound(String resourcePath) {
        // 1. 加载音频文件（路径需正确）
        URL resource = ResourceUtil.getResource(resourcePath);
        if (resource == null) {
            throw new RuntimeException("音频文件未找到");
        }
        Platform.runLater(() -> {
            try {
                // 如果当前有正在播放的音频则跳过
                if (isPlaying()) {
                    //停止当前运行
                    stopCurrentPlayback();
                }
                // 2. 创建 Media 对象（此时 Media 应为具体类）
                Media media = new Media(resource.toString());
                currentPlayer = new MediaPlayer(media);
                currentPlayer.setOnError(() -> {
                    currentPlayer = null;
                });
                currentPlayer.setOnEndOfMedia(() -> {
                    currentPlayer.dispose();
                    currentPlayer = null;
                });
                currentPlayer.play();
            } catch (Exception e) {
                currentPlayer = null;
            }
        });
    }

    public static boolean isPlaying() {
        return currentPlayer != null
                && currentPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public static void stopCurrentPlayback() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
    }

    public static void main(String[] args) {
        playSound();
    }
}
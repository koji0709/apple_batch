package com.sgswit.fx.utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.application.Platform;

public class SoundUtil {
    private static MediaPlayer currentPlayer;

    public static void playSound(String resourcePath) {
        Platform.runLater(() -> {
            try {
                // 如果当前有正在播放的音频则跳过
                if (isPlaying()) {
                    return;
                }

                Media sound = new Media(SoundUtil.class.getResource(resourcePath).toExternalForm());
                currentPlayer = new MediaPlayer(sound);
                currentPlayer.setOnError(() -> {
                    System.err.println("音频播放错误: " + currentPlayer.getError().getMessage());
                    currentPlayer = null;
                });
                currentPlayer.setOnEndOfMedia(() -> {
                    currentPlayer.dispose();
                    currentPlayer = null;
                });
                currentPlayer.play();
            } catch (Exception e) {
                System.err.println("无法加载音频文件: " + e.getMessage());
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
}
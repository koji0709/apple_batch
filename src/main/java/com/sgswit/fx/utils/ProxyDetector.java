package com.sgswit.fx.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ProxyDetector {

    public static void main(String[] args) {
        int[] portsToCheck = {8888, 8889}; // Charles 和 Fiddler 的默认端口
        String localHost = "127.0.0.1";   // 检查本地端口

        for (int port : portsToCheck) {
            if (isPortListening(localHost, port)) {
                System.out.println("抓包工具可能正在运行，监听端口: " + port);
            } else {
                System.out.println("端口 " + port + " 未被占用");
            }
        }

        checkSystemProxy();

        if (isProcessRunning("Charles") || isProcessRunning("Fiddler")) {
            System.out.println("检测到抓包工具正在运行");
        } else {
            System.out.println("未检测到抓包工具进程");
        }
    }

    /**
     * 检查软件默认端口是否被占用
     */
    public static boolean isPortListening(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 200); // 尝试连接到指定的端口
            return true; // 端口正在监听
        } catch (IOException e) {
            return false; // 端口未被监听
        }
    }

    /**
     * 检查当前系统的代理设置
     */
    public static void checkSystemProxy() {
        String httpProxyHost = System.getProperty("http.proxyHost");
        String httpProxyPort = System.getProperty("http.proxyPort");
        String httpsProxyHost = System.getProperty("https.proxyHost");
        String httpsProxyPort = System.getProperty("https.proxyPort");

        if (httpProxyHost != null || httpsProxyHost != null) {
            System.out.println("系统代理已启用:");
            System.out.println("HTTP 代理: " + httpProxyHost + ":" + httpProxyPort);
            System.out.println("HTTPS 代理: " + httpsProxyHost + ":" + httpsProxyPort);
        } else {
            System.out.println("未检测到系统代理");
        }
    }

    /**
     * 检测进程列表
     */
    public static boolean isProcessRunning(String processName) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            Process process;
            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("tasklist");
            } else if (os.contains("mac")) {
                process = Runtime.getRuntime().exec("ps aux");
            } else {
                process = Runtime.getRuntime().exec("ps -ef");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(processName.toLowerCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}

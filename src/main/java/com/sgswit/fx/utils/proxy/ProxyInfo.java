package com.sgswit.fx.utils.proxy;

public class ProxyInfo {
    private String host;
    private int    port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ProxyInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}

package com.sgswit.fx.utils.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * @author DeZh
 * @title: ProxyAuthenticator
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/3/2816:35
 */
public class ProxyAuthenticator extends Authenticator {
    private String proxyUser, proxyPass;

    public ProxyAuthenticator(String proxyUser, String proxyPass) {
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if (getRequestorType() == RequestorType.PROXY) {
            return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
        }
        return super.getPasswordAuthentication();
    }
}

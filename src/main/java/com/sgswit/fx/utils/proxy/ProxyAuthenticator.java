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
    private String user, password;

    public ProxyAuthenticator(String user, String password) {
        this.user     = user;
        this.password = password;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password.toCharArray());
    }
}

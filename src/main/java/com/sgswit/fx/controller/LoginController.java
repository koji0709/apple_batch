package com.sgswit.fx.controller;

import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.HostServicesUtil;
import com.sgswit.fx.utils.StageUtil;

/**
 * 登陆controller
 */
public class LoginController {

    public void login(){

        // todo

        // login success
        StageUtil.show(StageEnum.MAIN);
        StageUtil.close(StageEnum.LOGIN);
    }

    public void showDocument(){
        String url = "https://qm.qq.com/q/soD5j3tlEk";
        HostServicesUtil.getHostServices().showDocument(url); // 在默认
    }

}

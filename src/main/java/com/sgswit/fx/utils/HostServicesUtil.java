package com.sgswit.fx.utils;

import javafx.application.HostServices;

/**
 *
 */
public class HostServicesUtil {

    public static HostServices hostService;

    public static void setHostServices(HostServices hostServices){
        hostService = hostServices;
    }

    public static HostServices getHostServices(){
        return hostService;
    }

}

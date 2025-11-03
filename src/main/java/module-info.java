open module com.sgswit.fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires com.dlsc.formsfx;

    requires cn.hutool;
    requires javafaker;
    requires dd.plist;
    requires JsoupXpath;
    requires generex;
    requires commons.configuration;
    requires java.desktop;
    requires org.apache.commons.lang3;
    requires org.jsoup;
    requires javafx.media;
    requires jna;
    requires org.bouncycastle.provider;
    requires java.sql;
    requires d4ocr;
    requires commons.lang;
    requires fastjson;
    requires jdk.httpserver;
    exports com.sgswit.fx;
    exports com.sgswit.fx.utils.stage;
    exports com.sgswit.fx.controller;
}

package com.sgswit.fx.controller.iTunes.vo;

import cn.hutool.json.JSONObject;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.ImageView;

public class AppstoreItemVo {
    private SimpleIntegerProperty seq = new SimpleIntegerProperty();
    private SimpleStringProperty trackName = new SimpleStringProperty();
    private SimpleStringProperty price  = new SimpleStringProperty();
    private SimpleBooleanProperty select  = new SimpleBooleanProperty();
    private ImageView iconImage = new ImageView();
    private JSONObject trackJson;

    public int getSeq() {
        return seq.get();
    }

    public SimpleIntegerProperty seqProperty() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq.set(seq);
    }

    public String getTrackName() {
        return trackName.get();
    }

    public SimpleStringProperty trackNameProperty() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName.set(trackName);
    }

    public String getPrice() {
        return price.get();
    }

    public SimpleStringProperty priceProperty() {
        return price;
    }

    public void setPrice(String price) {
        this.price.set(price);
    }

    public boolean isSelect() {
        return select.get();
    }

    public SimpleBooleanProperty selectProperty() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select.set(select);
    }

    public ImageView getIconImage() {
        return iconImage;
    }

    public void setIconImage(ImageView iconImage) {
        this.iconImage = iconImage;
    }

    public JSONObject getTrackJson() {
        return trackJson;
    }

    public void setTrackJson(JSONObject trackJson) {
        this.trackJson = trackJson;
    }
}

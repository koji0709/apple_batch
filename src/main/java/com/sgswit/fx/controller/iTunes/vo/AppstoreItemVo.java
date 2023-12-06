package com.sgswit.fx.controller.iTunes.vo;

import cn.hutool.json.JSONObject;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.ImageView;

public class AppstoreItemVo {
    private SimpleIntegerProperty seq = new SimpleIntegerProperty();
    private SimpleStringProperty trackId = new SimpleStringProperty();
    private SimpleStringProperty trackName = new SimpleStringProperty();
    private SimpleStringProperty price  = new SimpleStringProperty();
    private SimpleStringProperty artworkUrl100  = new SimpleStringProperty();
    private SimpleStringProperty url  = new SimpleStringProperty();
    private SimpleBooleanProperty select  = new SimpleBooleanProperty();
    private ImageView iconImage = new ImageView();
    private JSONObject metadata;

    public int getSeq() {
        return seq.get();
    }

    public SimpleIntegerProperty seqProperty() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq.set(seq);
    }

    public String getTrackId() {
        return trackId.get();
    }

    public SimpleStringProperty trackIdProperty() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId.set(trackId);
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

    public String getArtworkUrl100() {
        return artworkUrl100.get();
    }

    public SimpleStringProperty artworkUrl100Property() {
        return artworkUrl100;
    }

    public void setArtworkUrl100(String artworkUrl100) {
        this.artworkUrl100.set(artworkUrl100);
    }

    public String getUrl() {
        return url.get();
    }

    public SimpleStringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
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

    public JSONObject getMetadata() {
        return metadata;
    }

    public void setMetadata(JSONObject metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "AppstoreItemVo{" +
                "seq=" + seq +
                ", trackId=" + trackId +
                ", trackName=" + trackName +
                ", price=" + price +
                ", artworkUrl100=" + artworkUrl100 +
                ", url=" + url +
                ", select=" + select +
                ", iconImage=" + iconImage +
                ", metadata=" + metadata +
                '}';
    }
}

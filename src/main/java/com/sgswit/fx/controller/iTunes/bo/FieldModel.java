package com.sgswit.fx.controller.iTunes.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author DeZh
 * @title: Fields
 * @projectName appleBatchService
 * @description: TODO
 * @date 2023/9/2114:46
 */
public class FieldModel {
    protected String id;
    protected String type;
    protected boolean required;
    protected String title;
    protected String placeholder;
    protected List<Map<String,String>> values;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public List<Map<String, String>> getValues() {
        if(null==values){
            values=new ArrayList<>();
        }
        return values;
    }

    public void setValues(List<Map<String, String>> values) {
        this.values = values;
    }
}

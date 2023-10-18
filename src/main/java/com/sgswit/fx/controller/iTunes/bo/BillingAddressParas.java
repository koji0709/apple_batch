package com.sgswit.fx.controller.iTunes.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DeZh
 * @title: BillingAddressParas
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/2420:57
 */
public class BillingAddressParas {
    public static class Paras {
        private String key;
        private String value;
        private String path;
        private String type;
        public Paras(String key, String value, String path,String type) {
            this.key = key;
            this.value = value;
            this.path = path;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
    public static List<BillingAddressParas.Paras> paras=new ArrayList<>(){{
        add(new BillingAddressParas.Paras("firstName","姓氏","ownerName_firstName","text"));
        add(new BillingAddressParas.Paras("lastName","名字","ownerName_lastName","text"));
        add(new BillingAddressParas.Paras("phoneAreaCode","区号","phoneNumber_areaCode","text"));
        add(new BillingAddressParas.Paras("phoneNumber","电话号","phoneNumber_number","text"));
        add(new BillingAddressParas.Paras("countryCode","网路拨号","phoneNumber_countryCode","text"));
        add(new BillingAddressParas.Paras("line1","街名和门牌号","billingAddress_line1","text"));
        add(new BillingAddressParas.Paras("line2","楼号、单元号、房间号","billingAddress_line2","text"));
        add(new BillingAddressParas.Paras("line3","街","billingAddress_line3","text"));
        add(new BillingAddressParas.Paras("suburb","郊区","billingAddress_suburb","text"));
        add(new BillingAddressParas.Paras("county","县市","billingAddress_county","text"));
        add(new BillingAddressParas.Paras("city","城市","billingAddress_city","text"));
        add(new BillingAddressParas.Paras("country","国家/地区","billingAddress_countryCode","text"));
        add(new BillingAddressParas.Paras("postalCode","邮政编码","billingAddress_postalCode","text"));
        add(new BillingAddressParas.Paras("stateProvince","省","billingAddress_stateProvinceName","dropdown"));
    }};

    public static BillingAddressParas.Paras getParasInfoByKey(String key){
        List<BillingAddressParas.Paras> list= paras.stream().filter(n->n.getKey().equals(key)).collect(Collectors.toList());
        return (list.size()==0)?null:list.get(0);
    }
    public static BillingAddressParas.Paras getParasInfoByPath(String key){
        List<BillingAddressParas.Paras> list= paras.stream().filter(n->n.getPath().equals(key)).collect(Collectors.toList());
        return (list.size()==0)?null:list.get(0);
    }
    public static boolean hasObjByPath(String key){
        List<BillingAddressParas.Paras> list= paras.stream().filter(n->n.getPath().equals(key)).collect(Collectors.toList());
        return (list.size()==0)?false:true;
    }
}

package com.sgswit.fx.constant;

import cn.hutool.core.util.StrUtil;
import java.util.*;

public class StoreFontsUtils {

    public static final Map<String,String> STORE_FRONTS = new LinkedHashMap<>();
    static {
        STORE_FRONTS.put("143481", "AE-阿拉伯联合酋长国");
        STORE_FRONTS.put("143540", "AG-安提瓜和巴布达");
        STORE_FRONTS.put("143538", "AI-安圭拉岛");
        STORE_FRONTS.put("143575", "AL-阿尔巴尼亚");
        STORE_FRONTS.put("143524", "AM-亚美尼亚");
        STORE_FRONTS.put("143564", "AO-安哥拉");
        STORE_FRONTS.put("143505", "AR-阿根廷");
        STORE_FRONTS.put("143445", "AT-奥地利");
        STORE_FRONTS.put("143460", "AU-澳大利亚");
        STORE_FRONTS.put("143568", "AZ-阿塞拜疆");
        STORE_FRONTS.put("143541", "BB-巴巴多斯");
        STORE_FRONTS.put("143490", "BD-孟加拉国");
        STORE_FRONTS.put("143446", "BE-比利时");
        STORE_FRONTS.put("143526", "BG-保加利亚");
        STORE_FRONTS.put("143559", "BH-巴林");
        STORE_FRONTS.put("143542", "BM-百慕大");
        STORE_FRONTS.put("143560", "BN-文莱");
        STORE_FRONTS.put("143556", "BO-玻利维亚");
        STORE_FRONTS.put("143503", "BR-巴西");
        STORE_FRONTS.put("143539", "BS-巴哈马");
        STORE_FRONTS.put("143525", "BW-博茨瓦纳");
        STORE_FRONTS.put("143565", "BY-白俄罗斯");
        STORE_FRONTS.put("143555", "BZ-伯利兹");
        STORE_FRONTS.put("143455", "CA-加拿大");
        STORE_FRONTS.put("143459", "CH-瑞士");
        STORE_FRONTS.put("143527", "CI-科特迪瓦");
        STORE_FRONTS.put("143483", "CL-智利");
        STORE_FRONTS.put("143465", "CN-中国大陆");
        STORE_FRONTS.put("143501", "CO-哥伦比亚");
        STORE_FRONTS.put("143495", "CR-哥斯达黎加");
        STORE_FRONTS.put("143557", "CY-塞浦路斯");
        STORE_FRONTS.put("143489", "CZ-捷克");
        STORE_FRONTS.put("143443", "DE-德国");
        STORE_FRONTS.put("143458", "DK-丹麦");
        STORE_FRONTS.put("143545", "DM-多米尼克");
        STORE_FRONTS.put("143508", "DO-多米尼加共和国");
        STORE_FRONTS.put("143563", "DZ-阿尔及利亚");
        STORE_FRONTS.put("143509", "EC-厄瓜多尔");
        STORE_FRONTS.put("143518", "EE-爱沙尼亚");
        STORE_FRONTS.put("143516", "EG-埃及");
        STORE_FRONTS.put("143454", "ES-西班牙");
        STORE_FRONTS.put("143447", "FI-芬兰");
        STORE_FRONTS.put("143442", "FR-法国");
        STORE_FRONTS.put("143444", "GB-英国");
        STORE_FRONTS.put("143546", "GD-格林纳达");
        STORE_FRONTS.put("143615", "GE-格鲁吉亚");
        STORE_FRONTS.put("143573", "GH-加纳");
        STORE_FRONTS.put("143448", "GR-希腊");
        STORE_FRONTS.put("143504", "GT-危地马拉");
        STORE_FRONTS.put("143553", "GY-圭亚那");
        STORE_FRONTS.put("143463", "HK-香港");
        STORE_FRONTS.put("143510", "HN-洪都拉斯");
        STORE_FRONTS.put("143494", "HR-克罗地亚");
        STORE_FRONTS.put("143482", "HU-匈牙利");
        STORE_FRONTS.put("143476", "ID-印度尼西亚");
        STORE_FRONTS.put("143449", "IE-爱尔兰");
        STORE_FRONTS.put("143491", "IL-以色列");
        STORE_FRONTS.put("143467", "IN-印度");
        STORE_FRONTS.put("143558", "IS-冰岛");
        STORE_FRONTS.put("143450", "IT-意大利");
        STORE_FRONTS.put("143511", "JM-牙买加");
        STORE_FRONTS.put("143528", "JO-约旦");
        STORE_FRONTS.put("143462", "JP-日本");
        STORE_FRONTS.put("143529", "KE-肯尼亚");
        STORE_FRONTS.put("143548", "KN-圣基茨和尼维斯");
        STORE_FRONTS.put("143466", "KR-韩国");
        STORE_FRONTS.put("143493", "KW-科威特");
        STORE_FRONTS.put("143544", "KY-开曼群岛");
        STORE_FRONTS.put("143517", "KZ-哈萨克斯坦");
        STORE_FRONTS.put("143497", "LB-黎巴嫩");
        STORE_FRONTS.put("143549", "LC-圣卢西亚");
        STORE_FRONTS.put("143522", "LI-列支敦士登");
        STORE_FRONTS.put("143486", "LK-斯里兰卡");
        STORE_FRONTS.put("143520", "LT-立陶宛");
        STORE_FRONTS.put("143451", "LU-卢森堡");
        STORE_FRONTS.put("143519", "LV-拉脱维亚");
        STORE_FRONTS.put("143523", "MD-摩尔多瓦");
        STORE_FRONTS.put("143531", "MG-马达加斯加");
        STORE_FRONTS.put("143530", "MK-北马其顿");
        STORE_FRONTS.put("143532", "ML-马里");
        STORE_FRONTS.put("143592", "MN-蒙古");
        STORE_FRONTS.put("143515", "MO-澳门");
        STORE_FRONTS.put("143547", "MS-蒙特塞拉特");
        STORE_FRONTS.put("143521", "MT-马耳他");
        STORE_FRONTS.put("143533", "MU-毛里求斯");
        STORE_FRONTS.put("143488", "MV-马尔代夫");
        STORE_FRONTS.put("143468", "MX-墨西哥");
        STORE_FRONTS.put("143473", "MY-马来西亚");
        STORE_FRONTS.put("143534", "NE-尼日尔");
        STORE_FRONTS.put("143561", "NG-尼日利亚");
        STORE_FRONTS.put("143512", "NI-尼加拉瓜");
        STORE_FRONTS.put("143452", "NL-荷兰");
        STORE_FRONTS.put("143457", "NO-挪威");
        STORE_FRONTS.put("143484", "NP-尼泊尔");
        STORE_FRONTS.put("143461", "NZ-新西兰");
        STORE_FRONTS.put("143562", "OM-阿曼");
        STORE_FRONTS.put("143485", "PA-巴拿马");
        STORE_FRONTS.put("143507", "PE-秘鲁");
        STORE_FRONTS.put("143474", "PH-菲律宾");
        STORE_FRONTS.put("143477", "PK-巴基斯坦");
        STORE_FRONTS.put("143478", "PL-波兰");
        STORE_FRONTS.put("143453", "PT-葡萄牙");
        STORE_FRONTS.put("143513", "PY-巴拉圭");
        STORE_FRONTS.put("143498", "QA-卡塔尔");
        STORE_FRONTS.put("143487", "RO-罗马尼亚");
        STORE_FRONTS.put("143500", "RS-塞尔维亚");
        STORE_FRONTS.put("143469", "RU-俄罗斯");
        STORE_FRONTS.put("143479", "SA-沙特阿拉伯");
        STORE_FRONTS.put("143456", "SE-瑞典");
        STORE_FRONTS.put("143464", "SG-新加坡");
        STORE_FRONTS.put("143499", "SI-斯洛文尼亚");
        STORE_FRONTS.put("143496", "SK-斯洛伐克");
        STORE_FRONTS.put("143535", "SN-塞内加尔");
        STORE_FRONTS.put("143554", "SR-苏里南");
        STORE_FRONTS.put("143506", "SV-萨尔瓦多");
        STORE_FRONTS.put("143552", "TC-特克斯和凯科斯群岛");
        STORE_FRONTS.put("143475", "TH-泰国");
        STORE_FRONTS.put("143536", "TN-突尼斯");
        STORE_FRONTS.put("143480", "TR-土耳其");
        STORE_FRONTS.put("143551", "TT-特立尼达和多巴哥");
        STORE_FRONTS.put("143470", "TW-台湾");
        STORE_FRONTS.put("143572", "TZ-坦桑尼亚");
        STORE_FRONTS.put("143492", "UA-乌克兰");
        STORE_FRONTS.put("143537", "UG-乌干达");
        STORE_FRONTS.put("143441", "US-美国");
        STORE_FRONTS.put("143514", "UY-乌拉圭");
        STORE_FRONTS.put("143566", "UZ-乌兹别克斯坦");
        STORE_FRONTS.put("143550", "VC-圣文森特和格林纳丁斯");
        STORE_FRONTS.put("143502", "VE-委内瑞拉");
        STORE_FRONTS.put("143543", "VG-英属维尔京群岛");
        STORE_FRONTS.put("143471", "VN-越南");
        STORE_FRONTS.put("143571", "YE-也门");
        STORE_FRONTS.put("143472", "ZA-南非");
    }

    public static String getCountryCodeFromStoreFront(String storeFront){
        List<String> sf = StrUtil.split(storeFront, "-");
        if(!sf.isEmpty()){
            String countryCode = getCountryCode(sf.get(0));

            return StrUtil.isNotEmpty(countryCode) ? countryCode.split("-")[0] : null;
        }
        return null;
    }

    public static String getCountryCode(String key){
        return STORE_FRONTS.get(key);
    }

    public static List<String> getCountryList(){
        List<String> countryList = new ArrayList<>();
        for (Map.Entry<String, String> entry : STORE_FRONTS.entrySet()) {
            countryList.add(entry.getValue());
        }
        return countryList;
    }

}

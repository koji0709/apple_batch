package com.sgswit.fx.constant;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DELL
 */
public class StoreFontsUtil {
    public static final Map<String,String> STORE_FRONTS = new HashMap<>();
    static {
        STORE_FRONTS.put("143481", "AE");
        STORE_FRONTS.put("143540", "AG");
        STORE_FRONTS.put("143538", "AI");
        STORE_FRONTS.put("143575", "AL");
        STORE_FRONTS.put("143524", "AM");
        STORE_FRONTS.put("143564", "AO");
        STORE_FRONTS.put("143505", "AR");
        STORE_FRONTS.put("143445", "AT");
        STORE_FRONTS.put("143460", "AU");
        STORE_FRONTS.put("143568", "AZ");
        STORE_FRONTS.put("143541", "BB");
        STORE_FRONTS.put("143490", "BD");
        STORE_FRONTS.put("143446", "BE");
        STORE_FRONTS.put("143526", "BG");
        STORE_FRONTS.put("143559", "BH");
        STORE_FRONTS.put("143542", "BM");
        STORE_FRONTS.put("143560", "BN");
        STORE_FRONTS.put("143556", "BO");
        STORE_FRONTS.put("143503", "BR");
        STORE_FRONTS.put("143539", "BS");
        STORE_FRONTS.put("143525", "BW");
        STORE_FRONTS.put("143565", "BY");
        STORE_FRONTS.put("143555", "BZ");
        STORE_FRONTS.put("143455", "CA");
        STORE_FRONTS.put("143459", "CH");
        STORE_FRONTS.put("143527", "CI");
        STORE_FRONTS.put("143483", "CL");
        STORE_FRONTS.put("143465", "CN");
        STORE_FRONTS.put("143501", "CO");
        STORE_FRONTS.put("143495", "CR");
        STORE_FRONTS.put("143557", "CY");
        STORE_FRONTS.put("143489", "CZ");
        STORE_FRONTS.put("143443", "DE");
        STORE_FRONTS.put("143458", "DK");
        STORE_FRONTS.put("143545", "DM");
        STORE_FRONTS.put("143508", "DO");
        STORE_FRONTS.put("143563", "DZ");
        STORE_FRONTS.put("143509", "EC");
        STORE_FRONTS.put("143518", "EE");
        STORE_FRONTS.put("143516", "EG");
        STORE_FRONTS.put("143454", "ES");
        STORE_FRONTS.put("143447", "FI");
        STORE_FRONTS.put("143442", "FR");
        STORE_FRONTS.put("143444", "GB");
        STORE_FRONTS.put("143546", "GD");
        STORE_FRONTS.put("143615", "GE");
        STORE_FRONTS.put("143573", "GH");
        STORE_FRONTS.put("143448", "GR");
        STORE_FRONTS.put("143504", "GT");
        STORE_FRONTS.put("143553", "GY");
        STORE_FRONTS.put("143463", "HK");
        STORE_FRONTS.put("143510", "HN");
        STORE_FRONTS.put("143494", "HR");
        STORE_FRONTS.put("143482", "HU");
        STORE_FRONTS.put("143476", "ID");
        STORE_FRONTS.put("143449", "IE");
        STORE_FRONTS.put("143491", "IL");
        STORE_FRONTS.put("143467", "IN");
        STORE_FRONTS.put("143558", "IS");
        STORE_FRONTS.put("143450", "IT");
        STORE_FRONTS.put("143511", "JM");
        STORE_FRONTS.put("143528", "JO");
        STORE_FRONTS.put("143462", "JP");
        STORE_FRONTS.put("143529", "KE");
        STORE_FRONTS.put("143548", "KN");
        STORE_FRONTS.put("143466", "KR");
        STORE_FRONTS.put("143493", "KW");
        STORE_FRONTS.put("143544", "KY");
        STORE_FRONTS.put("143517", "KZ");
        STORE_FRONTS.put("143497", "LB");
        STORE_FRONTS.put("143549", "LC");
        STORE_FRONTS.put("143522", "LI");
        STORE_FRONTS.put("143486", "LK");
        STORE_FRONTS.put("143520", "LT");
        STORE_FRONTS.put("143451", "LU");
        STORE_FRONTS.put("143519", "LV");
        STORE_FRONTS.put("143523", "MD");
        STORE_FRONTS.put("143531", "MG");
        STORE_FRONTS.put("143530", "MK");
        STORE_FRONTS.put("143532", "ML");
        STORE_FRONTS.put("143592", "MN");
        STORE_FRONTS.put("143515", "MO");
        STORE_FRONTS.put("143547", "MS");
        STORE_FRONTS.put("143521", "MT");
        STORE_FRONTS.put("143533", "MU");
        STORE_FRONTS.put("143488", "MV");
        STORE_FRONTS.put("143468", "MX");
        STORE_FRONTS.put("143473", "MY");
        STORE_FRONTS.put("143534", "NE");
        STORE_FRONTS.put("143561", "NG");
        STORE_FRONTS.put("143512", "NI");
        STORE_FRONTS.put("143452", "NL");
        STORE_FRONTS.put("143457", "NO");
        STORE_FRONTS.put("143484", "NP");
        STORE_FRONTS.put("143461", "NZ");
        STORE_FRONTS.put("143562", "OM");
        STORE_FRONTS.put("143485", "PA");
        STORE_FRONTS.put("143507", "PE");
        STORE_FRONTS.put("143474", "PH");
        STORE_FRONTS.put("143477", "PK");
        STORE_FRONTS.put("143478", "PL");
        STORE_FRONTS.put("143453", "PT");
        STORE_FRONTS.put("143513", "PY");
        STORE_FRONTS.put("143498", "QA");
        STORE_FRONTS.put("143487", "RO");
        STORE_FRONTS.put("143500", "RS");
        STORE_FRONTS.put("143469", "RU");
        STORE_FRONTS.put("143479", "SA");
        STORE_FRONTS.put("143456", "SE");
        STORE_FRONTS.put("143464", "SG");
        STORE_FRONTS.put("143499", "SI");
        STORE_FRONTS.put("143496", "SK");
        STORE_FRONTS.put("143535", "SN");
        STORE_FRONTS.put("143554", "SR");
        STORE_FRONTS.put("143506", "SV");
        STORE_FRONTS.put("143552", "TC");
        STORE_FRONTS.put("143475", "TH");
        STORE_FRONTS.put("143536", "TN");
        STORE_FRONTS.put("143480", "TR");
        STORE_FRONTS.put("143551", "TT");
        STORE_FRONTS.put("143470", "TW");
        STORE_FRONTS.put("143572", "TZ");
        STORE_FRONTS.put("143492", "UA");
        STORE_FRONTS.put("143537", "UG");
        STORE_FRONTS.put("143441", "US");
        STORE_FRONTS.put("143514", "UY");
        STORE_FRONTS.put("143566", "UZ");
        STORE_FRONTS.put("143550", "VC");
        STORE_FRONTS.put("143502", "VE");
        STORE_FRONTS.put("143543", "VG");
        STORE_FRONTS.put("143471", "VN");
        STORE_FRONTS.put("143571", "YE");
        STORE_FRONTS.put("143472", "ZA");
    }
    public static String getCountryCodeFromStoreFront(String storeFront){
        if(StringUtils.isEmpty(storeFront)){
            return null;
        }
        String[] ss = StringUtils.split(storeFront,"-");
        if(ss.length > 0){
            return STORE_FRONTS.get(ss[0]);
        }
        return null;
    }
}

package com.sgswit.fx.model;

import java.io.Serializable;

public class BaseAreaInfo implements Serializable {
    private String dialCode;
    /**
     * 英文名称
     */
    private String nameEn;

    /**
     * 中文名称
     */
    private String nameZh;

    private String code;

    /**
     * ISO2代码
     */
    private String code2;

    private static final long serialVersionUID = 1L;

    /**
     * @return dial_code
     */
    public String getDialCode() {
        return dialCode;
    }

    /**
     * @param dialCode
     */
    public void setDialCode(String dialCode) {
        this.dialCode = dialCode;
    }

    /**
     * 获取英文名称
     *
     * @return name_en - 英文名称
     */
    public String getNameEn() {
        return nameEn;
    }

    /**
     * 设置英文名称
     *
     * @param nameEn 英文名称
     */
    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    /**
     * 获取中文名称
     *
     * @return name_zh - 中文名称
     */
    public String getNameZh() {
        return nameZh;
    }

    /**
     * 设置中文名称
     *
     * @param nameZh 中文名称
     */
    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    /**
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取ISO2代码
     *
     * @return code2 - ISO2代码
     */
    public String getCode2() {
        return code2;
    }

    /**
     * 设置ISO2代码
     *
     * @param code2 ISO2代码
     */
    public void setCode2(String code2) {
        this.code2 = code2;
    }

}

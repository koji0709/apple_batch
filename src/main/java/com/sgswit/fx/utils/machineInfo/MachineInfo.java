package com.sgswit.fx.utils.machineInfo;

import java.io.Serializable;

public class MachineInfo implements Serializable {
    private int volumeSerialNumber; // 981679892 or String: 3A83-3F14

    private String macAddress; // 78-2B-CB-9A-65-ED

    private String processorName; // Intel(R) Core(TM) i5-2500 CPU @ 3.30GHz

    private String biosInfo; // DELL - 6222004 

    private String productId; // 00426-OEM-8992662-00400 or empty

    private String computerName; // YYH-PC

    private String hwProfile; // guid for hardware profile

    private String machineGuid;

    private String kMachineIdA;

    private String kMachineIdB;

    private static final long serialVersionUID = 8366549430935112994L;

    public int getVolumeSerialNumber() {
        return volumeSerialNumber;
    }

    public void setVolumeSerialNumber(int volumeSerialNumber) {
        machineGuid = null;
        this.volumeSerialNumber = volumeSerialNumber;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        machineGuid = null;
        this.macAddress = macAddress;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        machineGuid = null;
        this.processorName = processorName;
    }

    public String getBiosInfo() {
        return biosInfo;
    }

    public void setBiosInfo(String biosInfo) {
        machineGuid = null;
        this.biosInfo = biosInfo;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        machineGuid = null;
        this.productId = productId;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        machineGuid = null;
        this.computerName = computerName;
    }

    public String getHwProfile() {
        return hwProfile;
    }

    public void setHwProfile(String hwProfile) {
        machineGuid = null;
        this.hwProfile = hwProfile;
    }


    public String getkMachineIdA() {
        return kMachineIdA;
    }

    public void setkMachineIdA(String kMachineIdA) {
        this.kMachineIdA = kMachineIdA;
    }

    public String getkMachineIdB() {
        return kMachineIdB;
    }

    public void setkMachineIdB(String kMachineIdB) {
        this.kMachineIdB = kMachineIdB;
    }

    public String getMachineGuid() {
        return machineGuid;
    }

    public void setMachineGuid(String machineGuid) {
        this.machineGuid = machineGuid;
    }

    @Override
    public String toString() {
        return "MachineInfo{" + "volumeSerialNumber=" + volumeSerialNumber + ", macAddress='" + macAddress + '\''
                + ", processorName='" + processorName + '\'' + ", biosInfo='" + biosInfo + '\'' + ", productId='"
                + productId + '\'' + ", computerName='" + computerName + '\'' + ", hwProfile='" + hwProfile + '\''
                + ", machineGuid='" + machineGuid + '\'' + ", kMachineIdA='" + kMachineIdA + '\'' + ", kMachineIdB='"
                + kMachineIdB + '\'' + '}';
    }
}

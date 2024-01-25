package com.sgswit.fx.utils;

/**
 * @author DeZh
 * @title: SystemUtils
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/11/3010:41
 */
public class SystemUtils {
    /**
     * 判断操作系统是否是 Windows
     * @return true：操作系统是 Windows
     *         false：其它操作系统
     */
    public static boolean isWindows() {
        String osName = getOsName();
        return osName != null && osName.startsWith("Windows");
    }

    /**
     * 判断操作系统是否是 MacOS
     * @return true：操作系统是 MacOS
     *         false：其它操作系统
     */
    public static boolean isMacOs() {
        String osName = getOsName();
        return osName != null && osName.startsWith("Mac");
    }

    /**
     * 判断操作系统是否是 Linux
     * @return true：操作系统是 Linux
     *         false：其它操作系统
     */
    public static boolean isLinux() {
        String osName = getOsName();
        return (osName != null && osName.startsWith("Linux")) || (!isWindows() && !isMacOs());
    }
    public static boolean isVirtual() {
        String vmName = System.getProperty("java.vm.name");
        if (vmName != null && (vmName.contains("VMware") || vmName.contains("VirtualBox"))) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取操作系统名称
     * @return os.name 属性值
     */
    public static String getOsName() {
        return System.getProperty("os.name");
    }
}

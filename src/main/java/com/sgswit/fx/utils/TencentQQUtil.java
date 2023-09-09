package com.sgswit.fx.utils;

import java.util.HashMap;
import java.util.Map;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
/**
 * @author DeZh
 * @title: 获取客户端在线QQ，目前只使用于window
 * @projectName fx
 * @description: TODO
 * @date 2023/9/69:56
 */
public class TencentQQUtil {
    /*******QQ窗口文本内容前缀****eg：qqexchangewnd_shortcut_prefix_123456(其中123456即为qq号)*****/
    private static final String QQ_WINDOW_TEXT_PRE = "qqexchangewnd_shortcut_prefix_";

    private static final  User32 user32 = User32.INSTANCE;

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
        interface WndEnumProc extends StdCallCallback {
            boolean callback(Pointer hWnd, Pointer arg);
        }
        boolean EnumWindows(WndEnumProc lpEnumFunc, Pointer arg);

        int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
    }

    /******
     * 获取当前登录qq的信息
     * @return map集合
     */
    public static Map<String,String> getLoginQQList(){
        final Map<String,String> map = new HashMap<String,String>(5);

        user32.EnumWindows(new User32.WndEnumProc() {
            @Override
            public boolean callback(Pointer hWnd, Pointer userData) {
                byte[] windowText = new byte[512];
                user32.GetWindowTextA(hWnd, windowText, 512);
                String wText = Native.toString(windowText);
                if(_filterQQInfo(wText)){
                    map.put(hWnd.toString(), wText.substring(wText.indexOf(QQ_WINDOW_TEXT_PRE) + QQ_WINDOW_TEXT_PRE.length()));
                }
                return true;
            }
        }, null);
        return map;
    }


    /****
     * 过滤有效qq窗体信息
     * @param windowText
     * @return 是否为qq窗体信息
     */
    private static boolean _filterQQInfo(String windowText){

        if(windowText.startsWith(QQ_WINDOW_TEXT_PRE)){
            return true;
        }else{
            return false;
        }
    }


    public static void main(String[] args) {
        System.out.println(getLoginQQList());
    }
}

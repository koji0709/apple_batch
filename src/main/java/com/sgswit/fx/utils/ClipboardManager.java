package com.sgswit.fx.utils;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * @author DeZh
 * @title: ClipboardManager
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/2215:41
 */
public class ClipboardManager {
    /**
     * 将一个字符串设置到系统剪切板。
     *
     * @param str 要设置到剪切板的字符串。
     */
    public static void setClipboard(String str) throws IOException, UnsupportedFlavorException {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = new StringSelection(str);
        clipboard.setContents(transferable, null);
    }

    /**
     * 从系统剪切板获取一个字符串。
     *
     * @return 返回从剪切板获取到的字符串。
     */
    public static String getClipboard() throws IOException, UnsupportedFlavorException {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(null);
        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return (String) transferable.getTransferData(DataFlavor.stringFlavor);
        } else {
            throw new UnsupportedFlavorException(DataFlavor.stringFlavor);
        }
    }
}

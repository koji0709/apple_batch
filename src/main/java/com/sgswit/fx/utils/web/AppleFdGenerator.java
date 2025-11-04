package com.sgswit.fx.utils.web;/* AppleFdGenerator.java
 *
 * 将典型的 Apple X-Apple-I-FD-Client-Info 中 F 字段 JS 生成逻辑逐行移植到 Java。
 * 说明：
 *  - 若你有原始 JS 中的映射表 (y[], s[], 替换表等)，把它们替换到下方常量中以保证 100% 一致性。
 *  - 运行前请确保 JRE 支持 AWT（如果你启用屏幕信息采集）。如果在无头环境，可以注入 screen/canvas/webgl 值。
 *
 * 依赖：无外部库（纯 JDK），为了 JSON 输出我用简单拼接（可改为使用 Hutool/fastjson 等）。
 *
 * 用法示例：
 *  String F = AppleFdGenerator.generateF(ua, lang, tz, canvasHash, webglHash, extraBrowserFields);
 *  System.out.println(F);
 */

import java.awt.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class AppleFdGenerator {

    // ===========================
    // ====== PLACEHOLDERS ======
    // ===========================
    // 如果你有 JS 源里的原始映射表（y[], s[], 替换表等），把它们粘过来替换下列值。
    // 示例中给出了一个常见的 base64 风格字符表（可能与 Apple JS 不完全一致）。
    private static final String JS_ENC_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"; // 示例表（与 JS 中的 s[] 对应）
    // 如果 JS 中有特定的 byte->6bit 映射数组 y[]，请替换此处为 JS 中的数值序列（例如：private static final int[] JS_Y = {...};）
    private static final int[] JS_Y = null; // 默认 null，表示使用标准按位切分；若 JS 使用特殊映射则必须填充

    // 替换表：JS 中 w() 做了一系列字符串替换与混淆。若你能从 JS 中抽出替换数组，把 key->value 填在此 map 中以保证一致性。
    private static final Map<String, String> REPLACE_MAP = new LinkedHashMap<>(); // 按顺序执行替换
    static {
        // 示例（若已知真实替换规则，把真实规则放这里）
        REPLACE_MAP.put(";", "_");
        REPLACE_MAP.put("=", "-");
        REPLACE_MAP.put("&", ".");
        // 真实 JS 替换表示例可能包含更多项（按原始顺序执行）
    }

    // ===========================
    // ====== 公共方法 ======
    // ===========================

    /**
     * 生成完整的 X-Apple-I-FD-Client-Info 的 F 字段（近似或准确复刻）
     *
     * @param userAgent 浏览器 UA（原始）
     * @param lang 语言，如 "zh-CN"
     * @param tz 时区，如 "GMT+08:00"
     * @param canvasHash 来自浏览器 Canvas 指纹（必须与浏览器端的值一致以获得逐字符相同）
     * @param webglHash WebGL 指纹
     * @param extraFields 可选：浏览器端采集的额外字段（map），如果你能提供会增加相符概率
     * @return F 字段字符串
     */
    public static String generateF(String userAgent,
                                   String lang,
                                   String tz,
                                   String canvasHash,
                                   String webglHash,
                                   Map<String, String> extraFields) {

        // 1) 收集特征（按 JS gist 中定义的顺序非常关键）
        List<String> features = new ArrayList<>();

        // 常见浏览器可见字段（JS 里有很多，此处为核心集合，真实 JS 会更多）
        features.add(userAgent != null ? userAgent : "");
        features.add(lang != null ? lang : "");
        features.add(tz != null ? tz : "");
        features.add(System.getProperty("os.name", ""));
        features.add(System.getProperty("os.arch", ""));
        features.add(System.getProperty("os.version", ""));
        features.add(String.valueOf(Runtime.getRuntime().availableProcessors()));

        // 屏幕信息（如果在无头环境，请传入相应值）
        try {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            features.add(String.valueOf((int) d.getWidth()));
            features.add(String.valueOf((int) d.getHeight()));
        } catch (Throwable t) {
            features.add(""); features.add("");
        }

        // 主机名/小量网络信息（浏览器端会用 document.domain/cookie 等）
        try {
            features.add(InetAddress.getLocalHost().getHostName());
        } catch (Throwable t) {
            features.add("");
        }

        // 将浏览器端必需的 canvas/webgl 指纹插入（这两项在真实 Apple JS 中非常重要）
        features.add(canvasHash != null ? canvasHash : "");   // canvas hash
        features.add(webglHash != null ? webglHash : "");     // webgl hash

        // 额外字段（若你从浏览器采集到插件、fonts、cookieEnabled、navigator.platform、timezone offset 等，请放在 extraFields）
        if (extraFields != null && !extraFields.isEmpty()) {
            List<String> keys = new ArrayList<>(extraFields.keySet());
            Collections.sort(keys); // 保持稳定顺序
            for (String k : keys) {
                features.add(String.valueOf(extraFields.get(k)));
            }
        }

        // JS 有些占位符常用 @UTC@ / @CT@，用当前时间或随机数替换
        features.add("@UTC@");
        features.add("@CT@");

        // 2) 把特征按顺序拼接成字符串（JS gist 里通常以特定分隔符拼接，比如 ';' 或 '|')
        StringBuilder src = new StringBuilder();
        for (String f : features) {
            src.append(f == null ? "" : f);
            src.append(";"); // 使用 ; 作为分隔（与许多实现一致）
        }
        String base = src.toString();

        // 替换占位符
        base = base.replace("@UTC@", String.valueOf(System.currentTimeMillis()));
        base = base.replace("@CT@", String.valueOf(System.currentTimeMillis()));

        // 3) 自定义 6-bit 打包编码（对应 G() 函数）
        String encoded = custom6bitEncode(base);

        // 4) 后处理替换（对应 w() 里一系列替换），此步顺序敏感
        String post = applyReplaceMap(encoded);

        // 5) 附加校验/混淆（对应原 JS 里追加短校验串）
        String checksum = computeShortChecksum(post);

        // 6) 最终 F（gist 里通常会把 post + "." + checksum 或 post + checksum，视实现而定）
        String finalF = post + "." + checksum;

        return finalF;
    }

    // ===========================
    // ====== 实现细节函数 ======
    // ===========================

    /**
     * 将输入字节流做 6-bit 切片并映射到 JS_ENC_TABLE。如果 JS 使用特殊映射 JS_Y，请把 JS_Y 填入常量来改变映射逻辑。
     */
    private static String custom6bitEncode(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        // 若 JS 提供了特殊 y[] 映射（非逐位打切），这里应实现完全一致的映射逻辑
        if (JS_Y != null) {
            // 如果有 JS_Y（长度 256），把 bytes 中每个 byte 用 JS_Y 映射后再进行 6-bit 切片
            // 这个分支保留，但默认我们按位切片（更常见）
            return custom6bitEncodeWithY(bytes, JS_Y);
        }

        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bitsInBuffer = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsInBuffer += 8;
            while (bitsInBuffer >= 6) {
                bitsInBuffer -= 6;
                int index = (buffer >> bitsInBuffer) & 0x3F;
                out.append(JS_ENC_TABLE.charAt(index));
            }
        }
        if (bitsInBuffer > 0) {
            int index = (buffer << (6 - bitsInBuffer)) & 0x3F;
            out.append(JS_ENC_TABLE.charAt(index));
        }
        return out.toString();
    }

    private static String custom6bitEncodeWithY(byte[] bytes, int[] y) {
        // 示例：把每个原始字节按 y 映射成一个字节，然后把字节流按 6-bit 切片
        byte[] mapped = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            int ub = bytes[i] & 0xFF;
            int mv = (ub < y.length) ? y[ub] & 0xFF : ub;
            mapped[i] = (byte) mv;
        }

        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bitsInBuffer = 0;
        for (byte b : mapped) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsInBuffer += 8;
            while (bitsInBuffer >= 6) {
                bitsInBuffer -= 6;
                int index = (buffer >> bitsInBuffer) & 0x3F;
                out.append(JS_ENC_TABLE.charAt(index));
            }
        }
        if (bitsInBuffer > 0) {
            int index = (buffer << (6 - bitsInBuffer)) & 0x3F;
            out.append(JS_ENC_TABLE.charAt(index));
        }
        return out.toString();
    }

    /**
     * 应用示例替换表（保持插入顺序）
     */
    private static String applyReplaceMap(String s) {
        String out = s;
        for (Map.Entry<String, String> e : REPLACE_MAP.entrySet()) {
            out = out.replace(e.getKey(), e.getValue());
        }
        return out;
    }

    /**
     * 计算短校验字符串（模拟 JS 中的 crc/混淆步骤）。返回 3 字符短码（使用 JS_ENC_TABLE 映射）
     */
    private static String computeShortChecksum(String s) {
        // 我们用一个与 JS 思路类似的 16-bit 混淆算法（近似 gist 实现）
        int val = 0xFFFF;
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i) & 0xFF;
            val = ((val >>> 8) | (val << 8)) & 0xFFFF;
            val ^= c;
            val ^= (val & 0xFF) >>> 4;
            val ^= (val << 12) & 0xFFFF;
            val ^= ((val & 0xFF) << 5) & 0xFFFF;
            val &= 0xFFFF;
        }
        int a = (val >>> 12) & 0xF;
        int b = (val >>> 6) & 0x3F;
        int c = val & 0x3F;
        int ai = (a << 2) & 0x3F;
        return "" + JS_ENC_TABLE.charAt(ai) + JS_ENC_TABLE.charAt(b) + JS_ENC_TABLE.charAt(c);
    }

   public  static String getFdClientInfo(){
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0";
        String lang = "zh-CN";
        String tz = "GMT+08:00";

        // 示例 canvas/webgl 值（这些必须来自真实浏览器以保证一致）
        String canvasHash = "canvas:abcd1234ef56";   // 请替换为真实 canvas hash
        String webglHash = "webgl:xyz9876";         // 请替换为真实 webgl hash

        Map<String, String> extras = new HashMap<>();
        extras.put("navigator_platform", "Win32");
        extras.put("plugins", ""); // 浏览器端插件字符串（若有）

        String f = generateF(ua, lang, tz, canvasHash, webglHash, extras);

        // 输出：把 F 放入 JSON 的 F 字段即可
        String clientInfoJson = "{\"U\":\"" + ua.replace("\"","\\\"") + "\","
           + "\"L\":\"" + lang + "\","
           + "\"Z\":\"" + tz + "\","
           + "\"V\":\"1.1\","
           + "\"F\":\"" + f + "\"}";
        return clientInfoJson;
   }


}

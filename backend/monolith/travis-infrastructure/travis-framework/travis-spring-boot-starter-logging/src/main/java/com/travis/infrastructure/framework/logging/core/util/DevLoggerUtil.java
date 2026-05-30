package com.travis.infrastructure.framework.logging.core.util;

import org.slf4j.Logger;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 开发环境日志工具类
 * <p>
 * 用于在控制台以格式化的彩色边框输出日志信息，支持自动换行、中文宽度计算及 JSON 美化。
 * 仅用于开发环境下使用，便于调试和查看复杂数据结构。
 */
public class DevLoggerUtil {

    /**
     * JSON 序列化器，启用缩进输出
     */
    private static final ObjectMapper MAPPER = new ObjectMapper().rebuild()
            .enable(SerializationFeature.INDENT_OUTPUT).build();


    /**
     * 总输出宽度（字符数）
     */
    private static final int TOTAL_WIDTH = 80;


    /**
     * ANSI 颜色重置码
     */
    private static final String RESET = "\033[0m";
    /**
     * ANSI 青色码（用于键名）
     */
    private static final String CYAN = "\033[36m";
    /**
     * ANSI 绿色码（用于值）
     */
    private static final String GREEN = "\033[32m";
    /**
     * ANSI 黄色码（用于标题）
     */
    private static final String YELLOW = "\033[33m";

    /**
     * 正则表达式：匹配 ANSI 转义序列
     */
    private static final Pattern ANSI = Pattern.compile("\u001B\\[[;\\d]*m");

    /**
     * 打印格式化的日志信息
     *
     * @param logger SLF4J 日志记录器
     * @param title  日志标题
     * @param data   要打印的键值对数据
     */
    public static void print(Logger logger, String title, Map<String, Object> data) {
        var contentWidth = TOTAL_WIDTH - 4;
        // 计算所有键中最大的显示宽度（考虑中文字符占两格）
        var keyWidth = data.keySet().stream()
                .mapToInt(DevLoggerUtil::displayWidth)
                .max()
                .orElse(0);

        var border = "═".repeat(TOTAL_WIDTH - 2);

        var sb = new StringBuilder();
        sb.append("\n╔").append(border).append("╗\n");
        // 包装并添加标题
        sb.append(wrap(YELLOW + title + RESET, contentWidth)).append("\n");
        sb.append("╠").append(border).append("╣\n");

        for (var entry : data.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var coloredKey = CYAN + key + RESET;
            var visibleKey = displayWidth(key);

            // 对齐键名：如果当前键宽度小于最大宽度，则补充空格
            if (visibleKey < keyWidth) {
                coloredKey += " ".repeat(keyWidth - visibleKey);
            }

            var prefix = coloredKey + " : ";
            var indent = " ".repeat(keyWidth + 3);

            // 格式化值为多行字符串列表
            var lines = formatValue(value);
            for (int i = 0; i < lines.size(); i++) {
                String line;
                if (i == 0) {
                    // 第一行前缀为 "键 : "
                    line = prefix + GREEN + lines.get(i) + RESET;
                } else {
                    // 后续行使用缩进对齐
                    line = indent + GREEN + lines.get(i) + RESET;
                }
                // 自动换行并添加边框
                sb.append(wrap(line, contentWidth)).append("\n");
            }
        }
        sb.append("╚").append(border).append("╝");
        logger.info(sb.toString());
    }

    /**
     * 将对象格式化为字符串列表
     * <p>
     * 如果值是 Map 或 Collection，则序列化为美化的 JSON 并按行拆分；
     * 否则转换为字符串。若解析失败则返回原始字符串表示。
     *
     * @param value 待格式化的对象
     * @return 格式化后的字符串列表
     */
    private static List<String> formatValue(Object value) {
        if (value == null) return List.of("null");
        try {
            String json;
            if (value instanceof Map || value instanceof Collection) {
                json = MAPPER.writeValueAsString(value).trim();
            } else {
                json = String.valueOf(value).trim();
            }


            // 如果是 JSON 对象或数组，进行美化并分行
            if (json.startsWith("{") || json.startsWith("[")) {
                if (json.contains("\n")) {
                    return Arrays.asList(json.split("\n"));
                }
                return Arrays.asList(
                        MAPPER.readTree(json.trim())
                                .toPrettyString()
                                .split("\n"));
            }
            return List.of(json);
        } catch (Exception e) {
            // 异常时降级为普通字符串
            return List.of(String.valueOf(value));
        }
    }

    /**
     * 将内容包装在边框内，并根据可见宽度填充空格以达到指定宽度
     *
     * @param content 包含 ANSI 颜色码的内容
     * @param width   目标宽度（不含边框）
     * @return 包装后的字符串
     */
    private static String wrap(String content, int width) {
        var clean = stripAnsi(content);
        var visible = displayWidth(clean);
        var padding = width - visible;
        return "║ "
                + content
                + " ".repeat(Math.max(0, padding))
                + " ║";
    }

    /**
     * 移除字符串中的 ANSI 转义序列
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private static String stripAnsi(String text) {
        return text == null ? "" : ANSI.matcher(text).replaceAll("");
    }

    /**
     * 计算字符串的显示宽度
     * <p>
     * 中文字符计为 2 个单位，其他字符计为 1 个单位。
     *
     * @param text 输入文本
     * @return 显示宽度
     */
    private static int displayWidth(String text) {
        var clean = stripAnsi(text);
        int width = 0;
        for (int i = 0; i < clean.length(); i++) {
            if (isWideChar(clean.charAt(i))) {
                width += 2;
            } else {
                width += 1;
            }
        }
        return width;
    }

    /**
     * 判断字符是否为宽字符（如中文）
     *
     * @param c 待判断的字符
     * @return 如果是宽字符返回 true，否则 false
     */
    private static boolean isWideChar(char c) {
        var block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
                || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }
}

package com.xiaoming.common.utils;

import java.util.UUID;

/**
 * 字符串工具类
 * 提供常用的字符串处理方法
 *
 * @author xiaoming
 * @since 2024-01-01
 */
public class StringUtils {

    /**
     * 判断字符串是否为空
     *
     * @param str 待判断字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否非空
     *
     * @param str 待判断字符串
     * @return 是否非空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 生成UUID字符串（去除横线）
     *
     * @return UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 驼峰转下划线
     *
     * @param camelCaseStr 驼峰字符串
     * @return 下划线字符串
     */
    public static String camelToUnderscore(String camelCaseStr) {
        if (isEmpty(camelCaseStr)) {
            return camelCaseStr;
        }
        return camelCaseStr.replaceAll("[A-Z]", "_$0").toLowerCase();
    }

    /**
     * 下划线转驼峰
     *
     * @param underscoreStr 下划线字符串
     * @return 驼峰字符串
     */
    public static String underscoreToCamel(String underscoreStr) {
        if (isEmpty(underscoreStr)) {
            return underscoreStr;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : underscoreStr.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
}

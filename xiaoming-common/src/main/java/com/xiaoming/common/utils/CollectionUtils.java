package com.xiaoming.common.utils;

import java.util.Collection;

/**
 * 集合工具类
 * 提供常用的集合处理方法
 *
 * @author xiaoming
 * @since 2024-01-01
 */
public class CollectionUtils {

    /**
     * 判断集合是否为空
     *
     * @param collection 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否非空
     *
     * @param collection 集合
     * @return 是否非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
}

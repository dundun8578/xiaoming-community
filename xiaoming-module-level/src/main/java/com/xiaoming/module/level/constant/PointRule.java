package com.xiaoming.module.level.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 积分规则常量
 * 定义各种用户行为对应的积分奖励
 */
public class PointRule {

    /**
     * 积分规则配置
     * key: 行为标识
     * value: 积分值
     */
    public static final Map<String, Integer> POINT_RULE = new HashMap<>();

    static {
        // 发布笔记
        POINT_RULE.put("publish_note", 10);

        // 发布一级评论
        POINT_RULE.put("publish_comment", 2);

        // 发布二级评论
        POINT_RULE.put("publish_reply", 1);

        // 收到点赞
        POINT_RULE.put("like_received", 1);

        // 收到收藏
        POINT_RULE.put("collect_received", 2);

        // 收到关注
        POINT_RULE.put("follow_received", 5);

        // 关注他人
        POINT_RULE.put("follow_other", 1);

        // 笔记被浏览100次
        POINT_RULE.put("note_view_100", 5);

        // 每日签到
        POINT_RULE.put("daily_sign", 1);

        // 笔记获得精选
        POINT_RULE.put("note_featured", 50);

        // 连续签到7天
        POINT_RULE.put("sign_7_days", 20);

        // 连续签到30天
        POINT_RULE.put("sign_30_days", 100);
    }

    /**
     * 积分行为描述映射
     */
    public static final Map<String, String> ACTION_DESCRIPTION = new HashMap<>();

    static {
        ACTION_DESCRIPTION.put("publish_note", "发布笔记");
        ACTION_DESCRIPTION.put("publish_comment", "发布评论");
        ACTION_DESCRIPTION.put("publish_reply", "回复评论");
        ACTION_DESCRIPTION.put("like_received", "收到点赞");
        ACTION_DESCRIPTION.put("collect_received", "收到收藏");
        ACTION_DESCRIPTION.put("follow_received", "收到关注");
        ACTION_DESCRIPTION.put("follow_other", "关注用户");
        ACTION_DESCRIPTION.put("note_view_100", "笔记浏览量达标");
        ACTION_DESCRIPTION.put("daily_sign", "每日签到");
        ACTION_DESCRIPTION.put("note_featured", "笔记被精选");
        ACTION_DESCRIPTION.put("sign_7_days", "连续签到7天");
        ACTION_DESCRIPTION.put("sign_30_days", "连续签到30天");
    }

    /**
     * 获取积分规则
     */
    public static Integer getPoint(String action) {
        return POINT_RULE.getOrDefault(action, 0);
    }

    /**
     * 获取行为描述
     */
    public static String getDescription(String action) {
        return ACTION_DESCRIPTION.getOrDefault(action, action);
    }
}

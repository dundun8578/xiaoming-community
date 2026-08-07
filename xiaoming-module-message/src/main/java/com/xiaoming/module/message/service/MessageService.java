package com.xiaoming.module.message.service;

import java.util.List;

/**
 * 消息服务
 */
public interface MessageService {

    /**
     * 获取消息列表
     */
    List<Object> getMessages(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取未读消息数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记消息已读
     */
    void markRead(Long msgId);
}

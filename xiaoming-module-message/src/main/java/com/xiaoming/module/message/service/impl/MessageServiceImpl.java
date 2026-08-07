package com.xiaoming.module.message.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoming.module.message.entity.Message;
import com.xiaoming.module.message.mapper.MessageMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public List<Object> getMessages(Long userId, Integer pageNum, Integer pageSize) {
        return list(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .orderByDesc(Message::getCreateTime));
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return count(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    @Override
    public void markRead(Long msgId) {
        // 简化实现
    }
}

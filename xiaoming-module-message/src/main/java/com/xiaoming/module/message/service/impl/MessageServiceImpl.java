package com.xiaoming.module.message.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoming.module.message.entity.Message;
import com.xiaoming.module.message.mapper.MessageMapper;
import com.xiaoming.module.message.vo.MessageVO;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<MessageVO> getMessages(Long userId, Integer pageNum, Integer pageSize) {
        List<Message> messages = list(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .orderByDesc(Message::getCreateTime));

        return messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
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

    /**
     * 实体转 VO
     */
    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setFromUserId(message.getFromUserId());
        vo.setToUserId(message.getToUserId());
        vo.setType(message.getType());
        vo.setContent(message.getContent());
        vo.setTargetId(message.getTargetId());
        vo.setIsRead(message.getIsRead());
        if (message.getCreateTime() != null) {
            vo.setCreateTime(message.getCreateTime().format(TIME_FORMATTER));
        }
        return vo;
    }
}

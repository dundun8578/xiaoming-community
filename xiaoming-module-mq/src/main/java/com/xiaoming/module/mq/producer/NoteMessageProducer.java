package com.xiaoming.module.mq.producer;

import com.xiaoming.module.mq.constant.MQConstant;
import com.xiaoming.module.note.entity.Note;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息生产者
 */
@Component
public class NoteMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送笔记发布消息
     */
    public void sendNotePublishMessage(Note note) {
        rabbitTemplate.convertAndSend(
                MQConstant.NOTE_EXCHANGE,
                MQConstant.ROUTE_NOTE_PUBLISH,
                note
        );
    }

    /**
     * 发送笔记删除消息
     */
    public void sendNoteDeleteMessage(Long noteId) {
        rabbitTemplate.convertAndSend(
                MQConstant.NOTE_EXCHANGE,
                MQConstant.ROUTE_NOTE_DELETE,
                noteId
        );
    }
}

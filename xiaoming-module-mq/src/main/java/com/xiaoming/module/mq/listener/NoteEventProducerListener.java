package com.xiaoming.module.mq.listener;

import com.xiaoming.module.mq.producer.NoteMessageProducer;
import com.xiaoming.module.note.event.NoteDeletedEvent;
import com.xiaoming.module.note.event.NotePublishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听 note 模块发布的事件，转成 MQ 消息投递出去。
 *
 * 这一层存在的意义是「解耦」：note 模块不需要知道 MQ 的存在，
 * 它只负责发布事件；由这里决定要不要发消息、发到哪个交换机。
 */
@Slf4j
@Component
public class NoteEventProducerListener {

    @Autowired
    private NoteMessageProducer noteMessageProducer;

    @EventListener
    public void onNotePublished(NotePublishedEvent event) {
        try {
            noteMessageProducer.sendNotePublishMessage(event.getNote());
        } catch (Exception e) {
            // MQ 不可用不应该影响「发布笔记」这个主流程，降级为不投递即可
            log.error("投递笔记发布消息失败，已降级处理, noteId={}",
                    event.getNote() != null ? event.getNote().getId() : null, e);
        }
    }

    @EventListener
    public void onNoteDeleted(NoteDeletedEvent event) {
        try {
            noteMessageProducer.sendNoteDeleteMessage(event.getNoteId());
        } catch (Exception e) {
            log.error("投递笔记删除消息失败，已降级处理, noteId={}", event.getNoteId(), e);
        }
    }
}

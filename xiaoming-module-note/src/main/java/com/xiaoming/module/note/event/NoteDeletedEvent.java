package com.xiaoming.module.note.event;

import org.springframework.context.ApplicationEvent;

/**
 * 笔记删除事件
 *
 * 与 NotePublishedEvent 同理：note 模块只负责「喊一声」，
 * 由 mq 模块的监听器决定要不要投递消息、投到哪个队列。
 */
public class NoteDeletedEvent extends ApplicationEvent {

    private final Long noteId;

    public NoteDeletedEvent(Object source, Long noteId) {
        super(source);
        this.noteId = noteId;
    }

    public Long getNoteId() {
        return noteId;
    }
}

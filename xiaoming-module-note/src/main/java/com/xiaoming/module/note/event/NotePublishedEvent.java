package com.xiaoming.module.note.event;

import com.xiaoming.module.note.entity.Note;
import org.springframework.context.ApplicationEvent;

/**
 * 笔记发布事件
 *
 * 为什么用事件，而不是在 publishNote() 里直接调 MQ 生产者？
 * 因为 mq 模块为了拿到 Note 实体已经依赖了 note 模块，
 * note 再依赖 mq 就会形成循环依赖（Maven 直接构建失败）。
 *
 * 所以这里用 Spring 应用事件做解耦：
 *   note 模块只管「我发布了一条笔记」，至于谁去发 MQ 消息、发到哪，
 *   它完全不关心 —— 由 mq 模块的监听器去接。
 */
public class NotePublishedEvent extends ApplicationEvent {

    private final Note note;

    public NotePublishedEvent(Object source, Note note) {
        super(source);
        this.note = note;
    }

    public Note getNote() {
        return note;
    }
}

package com.xiaoming.module.note.event;
import com.xiaoming.module.note.entity.NoteOutbox;
import com.xiaoming.module.note.mapper.NoteOutboxMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
/** 同步监听：与笔记/互动操作共用数据库事务，避免数据库已提交但消息丢失。 */
@Component
public class NoteOutboxWriter {
    private final NoteOutboxMapper records;
    public NoteOutboxWriter(NoteOutboxMapper records){this.records=records;}
    private void add(Long noteId,String type){
        NoteOutbox row=new NoteOutbox();row.setNoteId(noteId);row.setEventType(type);
        row.setSent(0);row.setAttempts(0);row.setAvailableAt(LocalDateTime.now());row.setCreateTime(LocalDateTime.now());
        records.insert(row);
    }
    @EventListener public void published(NotePublishedEvent e){add(e.getNote().getId(),"PUBLISH");}
    @EventListener public void updated(NoteUpdatedEvent e){add(e.getNoteId(),"UPDATE");}
    @EventListener public void deleted(NoteDeletedEvent e){add(e.getNoteId(),"DELETE");}
}

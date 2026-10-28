package com.xiaoming.module.note.event;
import org.springframework.context.ApplicationEvent;
public class NoteUpdatedEvent extends ApplicationEvent {
    private final Long noteId;
    public NoteUpdatedEvent(Object source,Long noteId){super(source);this.noteId=noteId;}
    public Long getNoteId(){return noteId;}
}

package com.xiaoming.module.note.moderation;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoming.module.note.entity.*;
import com.xiaoming.module.note.mapper.NoteModerationMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
@Service
public class NoteModerationService {
    private final ContentModerator moderator;
    private final NoteModerationMapper records;
    public NoteModerationService(ContentModerator moderator,NoteModerationMapper records){
        this.moderator=moderator;this.records=records;
    }
    public ModerationResult review(Note note){
        ModerationResult result=moderator.review(note.getTitle(),note.getContent());
        note.setStatus(("SKIPPED".equals(result.getDecision()) || "APPROVE".equals(result.getDecision()))?1:
            ("REJECT".equals(result.getDecision())?4:3));
        return result;
    }
    public void save(Long noteId,ModerationResult result){
        NoteModeration record=new NoteModeration();record.setNoteId(noteId);
        record.setDecision(result.getDecision());record.setReason(result.getReason());
        record.setCreateTime(LocalDateTime.now());records.insert(record);
    }
    public NoteModeration latest(Long noteId){
        return records.selectOne(new LambdaQueryWrapper<NoteModeration>()
            .eq(NoteModeration::getNoteId,noteId).orderByDesc(NoteModeration::getId).last("LIMIT 1"));
    }
}

package com.xiaoming.module.note.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoming.module.note.entity.NoteOutbox;
import org.apache.ibatis.annotations.Select;
import java.util.List;
public interface NoteOutboxMapper extends BaseMapper<NoteOutbox> {
    @Select("SELECT * FROM note_outbox WHERE sent=0 AND available_at<=NOW() ORDER BY id LIMIT 10 FOR UPDATE SKIP LOCKED")
    List<NoteOutbox> lockPending();
}

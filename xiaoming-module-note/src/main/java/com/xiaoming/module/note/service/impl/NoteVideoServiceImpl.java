package com.xiaoming.module.note.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoming.module.note.entity.NoteVideo;
import com.xiaoming.module.note.mapper.NoteVideoMapper;
import org.springframework.stereotype.Service;

/**
 * 笔记视频服务实现
 */
@Service
public class NoteVideoServiceImpl extends ServiceImpl<NoteVideoMapper, NoteVideo> implements NoteVideoService {

    @Override
    public void saveVideo(Long noteId, NoteVideo video) {
        video.setNoteId(noteId);
        save(video);
    }
}

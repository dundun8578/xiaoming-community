package com.xiaoming.module.note.service;

import com.xiaoming.module.note.entity.NoteVideo;
import java.util.List;

/**
 * 笔记视频服务
 */
public interface NoteVideoService {

    /**
     * 保存视频
     */
    void saveVideo(Long noteId, NoteVideo video);
}

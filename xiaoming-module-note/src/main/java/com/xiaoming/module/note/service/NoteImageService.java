package com.xiaoming.module.note.service;

import java.util.List;

/**
 * 笔记图片服务
 */
public interface NoteImageService {

    /**
     * 批量保存图片
     */
    void batchSave(Long noteId, List<String> imageUrls);
}

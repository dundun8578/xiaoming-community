package com.xiaoming.module.note.service;
/** 由互动模块提供实现，避免 note 与 interaction 模块互相依赖。 */
public interface NoteInteractionReader {
    boolean isLiked(Long userId, Long noteId);
    boolean isCollected(Long userId, Long noteId);
}

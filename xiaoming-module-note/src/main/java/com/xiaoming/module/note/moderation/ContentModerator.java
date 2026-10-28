package com.xiaoming.module.note.moderation;
public interface ContentModerator {
    ModerationResult review(String title,String content);
}

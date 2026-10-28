package com.xiaoming.module.interaction.service;
import com.xiaoming.module.note.service.NoteInteractionReader;
import org.springframework.stereotype.Component;
@Component
public class NoteInteractionReaderImpl implements NoteInteractionReader {
    private final LikeService likes;
    private final CollectService collects;
    public NoteInteractionReaderImpl(LikeService likes, CollectService collects) { this.likes=likes;this.collects=collects; }
    public boolean isLiked(Long userId,Long noteId){return likes.isLiked(userId,noteId);}
    public boolean isCollected(Long userId,Long noteId){return collects.isCollected(userId,noteId);}
}

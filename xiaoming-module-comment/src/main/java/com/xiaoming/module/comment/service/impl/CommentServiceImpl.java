package com.xiaoming.module.comment.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.common.exception.GlobalExceptionEnum;
import com.xiaoming.module.comment.entity.Comment;
import com.xiaoming.module.comment.mapper.CommentMapper;
import com.xiaoming.module.comment.service.CommentService;
import com.xiaoming.module.comment.vo.CommentVO;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.note.mapper.NoteMapper;
import com.xiaoming.module.user.service.UserService;
import com.xiaoming.module.user.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.context.ApplicationEventPublisher events;
    private final NoteMapper notes;
    private final UserService users;
    public CommentServiceImpl(NoteMapper notes, UserService users) { this.notes=notes; this.users=users; }
    private Note requireNote(Long id) {
        Note note=notes.selectById(id);
        if (note==null || !Integer.valueOf(1).equals(note.getStatus()))
            throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_FOUND);
        return note;
    }
    @Override
    public List<CommentVO> listComments(Long noteId, int pageNum, int pageSize) {
        requireNote(noteId);
        return page(new Page<Comment>(pageNum,pageSize), new LambdaQueryWrapper<Comment>()
            .eq(Comment::getNoteId,noteId).orderByDesc(Comment::getId))
            .getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }
    @Override
    @Transactional(rollbackFor=Exception.class)
    public CommentVO addComment(Long noteId, Long userId, String content) {
        Note note=notes.lockById(noteId);
        if(note==null || !Integer.valueOf(1).equals(note.getStatus()))
            throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_FOUND);
        Comment comment=new Comment();
        comment.setNoteId(noteId); comment.setUserId(userId); comment.setContent(content);
        comment.setParentId(0L); comment.setLikeCount(0); comment.setIsDeleted(0);
        comment.setCreateTime(LocalDateTime.now());
        save(comment);
        notes.update(null,new LambdaUpdateWrapper<Note>().eq(Note::getId,noteId)
            .setSql("comment_count = comment_count + 1"));
        events.publishEvent(new com.xiaoming.module.note.event.NoteUpdatedEvent(this,noteId));
        return toVO(comment);
    }
    @Override
    @Transactional(rollbackFor=Exception.class)
    public void deleteComment(Long id, Long userId) {
        Comment comment=getById(id);
        if(comment==null) throw new BusinessException(GlobalExceptionEnum.COMMENT_NOT_FOUND);
        if(!userId.equals(comment.getUserId())) throw new BusinessException(GlobalExceptionEnum.FORBIDDEN);

        if(removeById(id)){
            notes.update(null,new LambdaUpdateWrapper<Note>().eq(Note::getId,comment.getNoteId())
                .setSql("comment_count = GREATEST(0, comment_count - 1)"));
            events.publishEvent(new com.xiaoming.module.note.event.NoteUpdatedEvent(this,comment.getNoteId()));
        }
    }
    private CommentVO toVO(Comment comment) {
        CommentVO vo=new CommentVO(); BeanUtils.copyProperties(comment,vo);
        UserVO user=users.getUserInfo(comment.getUserId());
        vo.setNickname(user==null?"已注销用户":user.getNickname());
        if(user!=null) vo.setAvatar(user.getAvatar());
        return vo;
    }
}

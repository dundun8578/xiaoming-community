package com.xiaoming.module.comment.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoming.module.comment.entity.Comment;
import com.xiaoming.module.comment.vo.CommentVO;
import java.util.List;
public interface CommentService extends IService<Comment> {
    List<CommentVO> listComments(Long noteId, int pageNum, int pageSize);
    CommentVO addComment(Long noteId, Long userId, String content);
    void deleteComment(Long commentId, Long userId);
}

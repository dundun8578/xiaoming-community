package com.xiaoming.module.comment.controller;
import com.xiaoming.common.result.Result;
import com.xiaoming.module.comment.dto.CommentCreateDTO;
import com.xiaoming.module.comment.service.CommentService;
import com.xiaoming.module.comment.vo.CommentVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.util.List;
@RestController
@Validated
@RequestMapping("/comment")
public class CommentController {
    private final CommentService comments;
    public CommentController(CommentService comments) { this.comments = comments; }
    @GetMapping("/note/{noteId}")
    public Result<List<CommentVO>> list(@PathVariable Long noteId,
            @RequestParam(defaultValue="1") @Min(1) int pageNum,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int pageSize) {
        return Result.ok(comments.listComments(noteId, pageNum, pageSize));
    }
    @PostMapping("/note/{noteId}")
    public Result<CommentVO> create(@PathVariable Long noteId,
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CommentCreateDTO dto) {
        return Result.ok(comments.addComment(noteId, userId, dto.getContent().trim()));
    }
    @DeleteMapping("/{commentId}")
    public Result<Void> delete(@PathVariable Long commentId,
            @RequestAttribute("userId") Long userId) {
        comments.deleteComment(commentId, userId);
        return Result.ok();
    }
}

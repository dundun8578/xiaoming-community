package com.xiaoming.module.note.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.note.dto.NotePublishDTO;
import com.xiaoming.module.note.dto.NoteUpdateDTO;
import com.xiaoming.module.note.service.NoteService;
import com.xiaoming.module.note.vo.NoteVO;
import io.swagger.v3.annotations.Api;
import io.swagger.v3.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 笔记控制器
 */
@RestController
@RequestMapping("/note")
{tags = "笔记模块")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping("/publish")
    public Result<NoteVO> publishNote(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody NotePublishDTO publishDTO) {
        NoteVO noteVO = noteService.publishNote(userId, publishDTO);
        return Result.ok(noteVO);
    }

    @PutMapping("/{noteId}")
    public Result<NoteVO> updateNote(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long noteId,
            @Valid @RequestBody NoteUpdateDTO updateDTO) {
        NoteVO noteVO = noteService.updateNote(userId, noteId, updateDTO);
        return Result.ok(noteVO);
    }

    @DeleteMapping("/{noteId}")
    public Result<Void> deleteNote(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long noteId) {
        noteService.deleteNote(userId, noteId);
        return Result.ok();
    }

    @GetMapping("/{noteId}")
    public Result<NoteVO> getNoteDetail(
            @PathVariable Long noteId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        NoteVO noteVO = noteService.getNoteDetail(noteId, userId);
        return Result.ok(noteVO);
    }

    @GetMapping("/user/{userId}")
    public Result<List<NoteVO>> getUserNotes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<NoteVO> notes = noteService.getUserNotes(userId, pageNum, pageSize);
        return Result.ok(notes);
    }

    @GetMapping("/hot")
    public Result<List<NoteVO>> getHotNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<NoteVO> notes = noteService.getHotNotes(pageNum, pageSize);
        return Result.ok(notes);
    }

    @GetMapping("/recommend")
    public Result<List<NoteVO>> getRecommendNotes(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<NoteVO> notes = noteService.getRecommendNotes(userId, pageNum, pageSize);
        return Result.ok(notes);
    }
}

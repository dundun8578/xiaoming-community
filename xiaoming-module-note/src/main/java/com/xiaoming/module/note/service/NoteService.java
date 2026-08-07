package com.xiaoming.module.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoming.module.note.dto.NotePublishDTO;
import com.xiaoming.module.note.dto.NoteUpdateDTO;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.note.vo.NoteVO;
import com.xiaoming.module.user.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 笔记服务接口
 */
public interface NoteService extends IService<Note> {

    /**
     * 发布笔记
     */
    NoteVO publishNote(Long userId, NotePublishDTO publishDTO);

    /**
     * 更新笔记
     */
    NoteVO updateNote(Long userId, Long noteId, NoteUpdateDTO updateDTO);

    /**
     * 删除笔记
     */
    void deleteNote(Long userId, Long noteId);

    /**
     * 获取笔记详情
     */
    NoteVO getNoteDetail(Long noteId, Long currentUserId);

    /**
     * 获取用户笔记列表
     */
    List<NoteVO> getUserNotes(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取热门笔记
     */
    List<NoteVO> getHotNotes(Integer pageNum, Integer pageSize);

    /**
     * 获取推荐笔记
     */
    List<NoteVO> getRecommendNotes(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 增加浏览数
     */
    void increaseViewCount(Long noteId);

    /**
     * 更新热度分数
     */
    void updateHotScore(Long noteId);
}

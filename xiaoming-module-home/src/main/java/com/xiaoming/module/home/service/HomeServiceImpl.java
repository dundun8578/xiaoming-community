package com.xiaoming.module.home.service;

import com.xiaoming.module.note.service.NoteService;
import com.xiaoming.module.note.vo.NoteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 首页服务实现
 */
@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private NoteService noteService;

    @Override
    public List<NoteVO> getHomeFeed(Long userId, int pageNum, int pageSize) {
        return noteService.getHotNotes(pageNum, pageSize);
    }
}

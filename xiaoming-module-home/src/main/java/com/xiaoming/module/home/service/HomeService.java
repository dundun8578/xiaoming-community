package com.xiaoming.module.home.service;

import com.xiaoming.module.note.vo.NoteVO;
import java.util.List;

/**
 * 首页服务
 */
public interface HomeService {

    /**
     * 获取首页推荐流
     */
    List<NoteVO> getHomeFeed(Long userId, int pageNum, int pageSize);
}

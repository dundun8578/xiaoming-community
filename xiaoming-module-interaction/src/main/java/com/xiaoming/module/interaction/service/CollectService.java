package com.xiaoming.module.interaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoming.module.interaction.entity.Collect;
import com.xiaoming.module.interaction.vo.CollectVO;
import java.util.List;

/**
 * 收藏服务
 */
public interface CollectService extends IService<Collect> {

    /**
     * 收藏/取消收藏
     */
    void toggleCollect(Long userId, Long noteId);

    /**
     * 检查是否已收藏
     */
    boolean isCollected(Long userId, Long noteId);

    /**
     * 获取用户收藏列表
     */
    List<CollectVO> getUserCollects(Long userId, Integer pageNum, Integer pageSize);
}

package com.xiaoming.module.interaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoming.module.interaction.entity.Like;
import java.util.List;

/**
 * 点赞服务
 */
public interface LikeService extends IService<Like> {

    /**
     * 点赞/取消点赞
     */
    void toggleLike(Long userId, Long noteId);

    /**
     * 检查是否已点赞
     */
    boolean isLiked(Long userId, Long noteId);

    /**
     * 获取点赞状态
     */
    boolean getLikeStatus(Long userId, Long noteId);
}

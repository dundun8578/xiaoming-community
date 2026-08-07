package com.xiaoming.module.level.consumer;

import com.xiaoming.module.level.service.UserPointService;
import com.xiaoming.module.level.constant.PointRule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 积分事件消费者
 * 监听各种用户行为事件，自动发放积分
 */
@Component
public class PointEventConsumer {

    @Autowired
    private UserPointService userPointService;

    /**
     * 监听笔记发布事件
     */
    @RabbitListener(queues = "point.note.publish.queue")
    public void handleNotePublish(Long userId) {
        addPoint(userId, "publish_note", null);
    }

    /**
     * 监听评论发布事件
     */
    @RabbitListener(queues = "point.comment.publish.queue")
    public void handleCommentPublish(Long userId, Long commentId, Integer commentType) {
        String action = commentType == 1 ? "publish_comment" : "publish_reply";
        addPoint(userId, action, commentId);
    }

    /**
     * 监听点赞事件
     */
    @RabbitListener(queues = "point.like.received.queue")
    public void handleLikeReceived(Long userId, Long noteId) {
        addPoint(userId, "like_received", noteId);
    }

    /**
     * 监听收藏事件
     */
    @RabbitListener(queues = "point.collect.received.queue")
    public void handleCollectReceived(Long userId, Long noteId) {
        addPoint(userId, "collect_received", noteId);
    }

    /**
     * 监听关注事件(被关注)
     */
    @RabbitListener(queues = "point.follow.received.queue")
    public void handleFollowReceived(Long userId, Long followerId) {
        addPoint(userId, "follow_received", followerId);
    }

    /**
     * 监听关注事件(主动关注)
     */
    @RabbitListener(queues = "point.follow.other.queue")
    public void handleFollowOther(Long userId, Long followUserId) {
        addPoint(userId, "follow_other", followUserId);
    }

    /**
     * 添加积分
     */
    private void addPoint(Long userId, String action, Long actionId) {
        Integer point = PointRule.getPoint(action);
        if (point == null || point <= 0) {
            return;
        }

        String description = PointRule.getDescription(action);
        userPointService.addPoint(userId, point, action, actionId, description);
    }
}

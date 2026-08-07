package com.xiaoming.module.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoming.common.constant.ResultCode;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.module.message.entity.Message;
import com.xiaoming.module.message.mapper.MessageMapper;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.note.mapper.NoteMapper;
import com.xiaoming.module.social.entity.Follow;
import com.xiaoming.module.social.mapper.FollowMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 笔记发布消费者
 */
@Component
public class NotePublishConsumer {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private MessageMapper messageMapper;

    @RabbitListener(queues = "note.publish.queue")
    public void handleNotePublish(Note note) {
        try {
            // 1. 更新ES索引 (Step8完成后取消注释)
            // elasticsearchService.saveNoteToEs(note);

            // 2. 推送关注者动态
            List<Follow> followers = followMapper.selectList(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowUserId, note.getUserId())
            );

            for (Follow follow : followers) {
                Message message = new Message();
                message.setFromUserId(note.getUserId());
                message.setToUserId(follow.getUserId());
                message.setType(4); // 关注动态
                message.setContent("你关注的用户发布了新笔记: " + note.getTitle());
                message.setTargetId(note.getId());
                message.setIsRead(0);
                messageMapper.insert(message);
            }

            System.out.println("笔记发布消息处理完成: noteId=" + note.getId());
        } catch (Exception e) {
            System.err.println("处理笔记发布消息失败: " + e.getMessage());
            throw e; // 抛出异常触发重试
        }
    }

    @RabbitListener(queues = "note.delete.queue")
    public void handleNoteDelete(Long noteId) {
        try {
            // 删除ES索引 (Step8完成后取消注释)
            // elasticsearchService.deleteNoteFromEs(noteId);

            System.out.println("笔记删除消息处理完成: noteId=" + noteId);
        } catch (Exception e) {
            System.err.println("处理笔记删除消息失败: " + e.getMessage());
            throw e;
        }
    }
}

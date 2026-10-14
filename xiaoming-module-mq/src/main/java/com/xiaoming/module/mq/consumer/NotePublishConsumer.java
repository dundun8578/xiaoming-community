package com.xiaoming.module.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoming.common.constant.ResultCode;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.module.message.entity.Message;
import com.xiaoming.module.message.mapper.MessageMapper;
import com.xiaoming.module.mq.constant.MQConstant;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.note.mapper.NoteMapper;
import com.xiaoming.module.search.service.NoteSearchService;
import com.xiaoming.module.social.entity.Follow;
import com.xiaoming.module.social.mapper.FollowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 笔记发布消费者
 */
@Slf4j
@Component
public class NotePublishConsumer {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private MessageMapper messageMapper;

    /** ES 搜索服务：把新发布的笔记同步进索引，否则搜索永远搜不到数据 */
    @Autowired
    private NoteSearchService noteSearchService;

    @RabbitListener(queues = MQConstant.NOTE_PUBLISH_QUEUE)
    public void handleNotePublish(Note note) {
        try {
            // 1. 同步 ES 索引
            // ES 属于增强能力：同步失败只记日志，不能让整条消息无限重试
            try {
                noteSearchService.saveNoteToEs(note);
            } catch (Exception e) {
                log.error("同步笔记到 ES 失败，已跳过, noteId={}", note.getId(), e);
            }

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

            log.info("笔记发布消息处理完成: noteId={}", note.getId());
        } catch (Exception e) {
            log.error("处理笔记发布消息失败, noteId={}", note.getId(), e);
            throw e; // 抛出异常触发重试
        }
    }

    @RabbitListener(queues = MQConstant.NOTE_DELETE_QUEUE)
    public void handleNoteDelete(Long noteId) {
        try {
            // 从 ES 删除索引
            try {
                noteSearchService.deleteNoteFromEs(noteId);
            } catch (Exception e) {
                log.error("从 ES 删除笔记失败，已跳过, noteId={}", noteId, e);
            }

            log.info("笔记删除消息处理完成: noteId={}", noteId);
        } catch (Exception e) {
            log.error("处理笔记删除消息失败, noteId={}", noteId, e);
            throw e;
        }
    }
}

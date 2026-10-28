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
    @org.springframework.transaction.annotation.Transactional(rollbackFor=Exception.class)
    public void handleNotePublish(Note note) {
        try {

            Note current = noteMapper.lockIncludingDeleted(note.getId());
            if(current==null || Integer.valueOf(1).equals(current.getIsDeleted()) || !Integer.valueOf(1).equals(current.getStatus())) {
                noteSearchService.deleteNoteFromEs(note.getId());return;
            }
            noteSearchService.saveNoteToEs(current);
            // 2. 推送关注者动态
            List<Follow> followers = followMapper.selectList(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowUserId, note.getUserId())
            );

            for (Follow follow : followers) {
                if(messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                    .eq(Message::getToUserId,follow.getUserId()).eq(Message::getTargetId,note.getId())
                    .eq(Message::getType,4))>0)continue;
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


    @RabbitListener(queues = MQConstant.NOTE_UPDATE_QUEUE)
    @org.springframework.transaction.annotation.Transactional(rollbackFor=Exception.class)
    public void syncLatest(Long noteId) {
        Note current=noteMapper.lockIncludingDeleted(noteId);
        if(current==null || !Integer.valueOf(1).equals(current.getStatus()))
            noteSearchService.deleteNoteFromEs(noteId);
        else noteSearchService.saveNoteToEs(current);
    }
    @RabbitListener(queues = MQConstant.NOTE_DELETE_QUEUE)
    @org.springframework.transaction.annotation.Transactional(rollbackFor=Exception.class)
    public void handleNoteDelete(Long noteId) {
        try {
            syncLatest(noteId);

            log.info("笔记删除消息处理完成: noteId={}", noteId);
        } catch (Exception e) {
            log.error("处理笔记删除消息失败, noteId={}", noteId, e);
            throw e;
        }
    }
}

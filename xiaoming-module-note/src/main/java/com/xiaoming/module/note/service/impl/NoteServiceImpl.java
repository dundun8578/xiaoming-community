package com.xiaoming.module.note.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.common.exception.GlobalExceptionEnum;
import com.xiaoming.module.note.dto.*;
import com.xiaoming.module.note.entity.*;
import com.xiaoming.module.note.event.*;
import com.xiaoming.module.note.mapper.*;
import com.xiaoming.module.note.vo.*;
import com.xiaoming.module.user.service.UserService;
import com.xiaoming.module.user.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper,Note> implements NoteService {
    @Autowired private com.xiaoming.module.note.moderation.NoteModerationService moderation;
    @Autowired private NoteImageMapper images;
    @Autowired private NoteVideoMapper videos;
    @Autowired private UserService users;
    @Autowired private StringRedisTemplate redis;
    @Autowired private ObjectMapper json;
    @Autowired private ApplicationEventPublisher events;
    @Autowired private ObjectProvider<NoteInteractionReader> interactions;
    private static final String HOT_PREFIX="hot:notes:size:";
    private void checkPage(Integer page,Integer size){
        if(page==null || size==null || page<1 || size<1 || size>100)
            throw new BusinessException(GlobalExceptionEnum.PARAM_ERROR);
    }
    private Note requireNote(Long id){
        Note note=getById(id);
        if(note==null)throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_FOUND);
        return note;
    }
    private Note ownNote(Long userId,Long id){
        Note note=baseMapper.lockById(id);
        if(note==null)throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_FOUND);
        if(!userId.equals(note.getUserId()))throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_OWNER);
        return note;
    }
    @Override @Transactional(rollbackFor=Exception.class)
    public NoteVO publishNote(Long userId,NotePublishDTO dto){
        Note note=new Note();
        note.setUserId(userId);note.setTitle(dto.getTitle().trim());note.setContent(dto.getContent());
        note.setCoverImage(dto.getCoverImage());note.setTags(dto.getTags());note.setType(dto.getType());
        note.setStatus(1);note.setIsDeleted(0);note.setLikeCount(0);note.setCollectCount(0);
        note.setCommentCount(0);note.setViewCount(0);note.setHotScore(0L);
        note.setPublishTime(LocalDateTime.now());
        com.xiaoming.module.note.moderation.ModerationResult review=moderation.review(note);
        save(note);moderation.save(note.getId(),review);
        if(Integer.valueOf(1).equals(note.getStatus()))events.publishEvent(new NotePublishedEvent(this,note));
        return toVO(note,userId);
    }
    @Override @Transactional(rollbackFor=Exception.class)
    public NoteVO updateNote(Long userId,Long noteId,NoteUpdateDTO dto){
        Note note=ownNote(userId,noteId);
        boolean wasPublished=Integer.valueOf(1).equals(note.getStatus());
        if(dto.getTitle()!=null){
            if(dto.getTitle().isBlank() || dto.getTitle().length()>200)throw new BusinessException(GlobalExceptionEnum.PARAM_ERROR);
            note.setTitle(dto.getTitle().trim());
        }
        if(dto.getContent()!=null){
            if(dto.getContent().isBlank())throw new BusinessException(GlobalExceptionEnum.PARAM_ERROR);
            note.setContent(dto.getContent());
        }
        if(dto.getCoverImage()!=null)note.setCoverImage(dto.getCoverImage());
        if(dto.getTags()!=null)note.setTags(dto.getTags());
        note.setUpdateTime(LocalDateTime.now());
        com.xiaoming.module.note.moderation.ModerationResult review=moderation.review(note);
        updateById(note);moderation.save(note.getId(),review);
        if(!wasPublished && Integer.valueOf(1).equals(note.getStatus()))events.publishEvent(new NotePublishedEvent(this,note));
        else events.publishEvent(new NoteUpdatedEvent(this,noteId));
        return toVO(note,userId);
    }
    @Override @Transactional(rollbackFor=Exception.class)
    public void deleteNote(Long userId,Long noteId){
        ownNote(userId,noteId);
        removeById(noteId);
        events.publishEvent(new NoteDeletedEvent(this,noteId));
    }
    @Override
    public NoteVO getNoteDetail(Long noteId,Long viewer){
        Note note=requireNote(noteId);
        if(!Integer.valueOf(1).equals(note.getStatus()) && !note.getUserId().equals(viewer))
            throw new BusinessException(GlobalExceptionEnum.NOTE_DELETED);
        increaseViewCount(noteId);note.setViewCount(note.getViewCount()+1);
        return toVO(note,viewer);
    }
    @Override
    public List<NoteVO> getUserNotes(Long userId,Integer pageNum,Integer pageSize){
        checkPage(pageNum,pageSize);
        return page(new Page<Note>(pageNum,pageSize),new LambdaQueryWrapper<Note>()
            .eq(Note::getUserId,userId).eq(Note::getStatus,1).orderByDesc(Note::getPublishTime).orderByDesc(Note::getId))
            .getRecords().stream().map(n->toVO(n,null)).collect(Collectors.toList());
    }
    @Override
    public List<NoteVO> getHotNotes(Integer pageNum,Integer pageSize){
        checkPage(pageNum,pageSize);
        String key=pageNum==1?HOT_PREFIX+pageSize:null;
        if(key!=null)try{
            String cached=redis.opsForValue().get(key);
            if(cached!=null)return json.readValue(cached,json.getTypeFactory().constructCollectionType(List.class,NoteVO.class));
        }catch(Exception ignored){}

        List<NoteVO> ranked=readRanked(pageNum,pageSize);
        List<NoteVO> result=ranked!=null?ranked:page(new Page<Note>(pageNum,pageSize),new LambdaQueryWrapper<Note>()
            .eq(Note::getStatus,1).orderByDesc(Note::getHotScore).orderByDesc(Note::getId))
            .getRecords().stream().map(n->toVO(n,null)).collect(Collectors.toList());
        if(key!=null)try{redis.opsForValue().set(key,json.writeValueAsString(result),300+(long)(Math.random()*60),TimeUnit.SECONDS);}
        catch(Exception ignored){}
        return result;
    }

    private List<NoteVO> readRanked(int pageNum,int pageSize){
        try{
            Set<String> ids=redis.opsForZSet().reverseRange("recommend:hot",(long)(pageNum-1)*pageSize,(long)pageNum*pageSize-1);
            if(ids==null || ids.isEmpty())return null;
            Map<Long,Note> records=listByIds(ids.stream().map(Long::valueOf).collect(Collectors.toList()))
                .stream().filter(n->Integer.valueOf(1).equals(n.getStatus())).collect(Collectors.toMap(Note::getId,n->n));
            if(records.size()!=ids.size())return null;
            return ids.stream().map(Long::valueOf).map(records::get).map(n->toVO(n,null)).collect(Collectors.toList());
        }catch(Exception e){return null;}
    }
    @Override
    public List<NoteVO> getRecommendNotes(Long userId,Integer pageNum,Integer pageSize){
        return getHotNotes(pageNum,pageSize);
    }
    @Override
    public void increaseViewCount(Long id){
        update(new LambdaUpdateWrapper<Note>().eq(Note::getId,id).eq(Note::getStatus,1).setSql("view_count = view_count + 1"));
    }
    @Override
    public void updateHotScore(Long id){
        Note note=getById(id);
        if(note==null)return;
        long score=note.getViewCount()+note.getLikeCount()*5L+note.getCollectCount()*10L+note.getCommentCount()*3L;
        update(new LambdaUpdateWrapper<Note>().eq(Note::getId,id).set(Note::getHotScore,score));
        evictHotCache();
    }
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT,fallbackExecution=true,
        classes={NotePublishedEvent.class,NoteUpdatedEvent.class,NoteDeletedEvent.class})
    public void onNoteChanged(ApplicationEvent event){evictHotCache();}
    public void evictHotCache(){
        try{redis.delete(java.util.stream.IntStream.rangeClosed(1,100).mapToObj(n->HOT_PREFIX+n).collect(Collectors.toList()));}
        catch(Exception ignored){}
    }
    private NoteVO toVO(Note note,Long viewer){
        NoteVO vo=new NoteVO();BeanUtils.copyProperties(note,vo);
        if(viewer!=null && viewer.equals(note.getUserId())) {
            NoteModeration record=moderation.latest(note.getId());
            if(record!=null){vo.setModerationStatus(record.getDecision());vo.setModerationReason(record.getReason());}
        }
        UserVO user=users.getUserInfo(note.getUserId());
        if(user!=null){vo.setAuthorName(user.getNickname());vo.setAuthorAvatar(user.getAvatar());}
        else vo.setAuthorName("已注销用户");
        List<NoteImage> imageList=images.selectList(new LambdaQueryWrapper<NoteImage>()
            .eq(NoteImage::getNoteId,note.getId()).orderByAsc(NoteImage::getSort));
        vo.setImages(imageList.stream().map(img->{NoteImageVO item=new NoteImageVO();BeanUtils.copyProperties(img,item);return item;})
            .collect(Collectors.toList()));
        NoteVideo video=videos.selectOne(new LambdaQueryWrapper<NoteVideo>().eq(NoteVideo::getNoteId,note.getId()).last("LIMIT 1"));
        if(video!=null){NoteVideoVO item=new NoteVideoVO();BeanUtils.copyProperties(video,item);vo.setVideo(item);}
        if(viewer!=null){
            NoteInteractionReader reader=interactions.getIfAvailable();
            vo.setIsLiked(reader!=null && reader.isLiked(viewer,note.getId()));
            vo.setIsCollected(reader!=null && reader.isCollected(viewer,note.getId()));
        }
        return vo;
    }
}

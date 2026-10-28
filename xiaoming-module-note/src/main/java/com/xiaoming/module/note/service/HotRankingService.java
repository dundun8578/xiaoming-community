package com.xiaoming.module.note.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.note.mapper.NoteMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Service
public class HotRankingService {
    private final NoteMapper notes;
    private final StringRedisTemplate redis;
    private final NoteServiceImpl service;
    public HotRankingService(NoteMapper notes,StringRedisTemplate redis,NoteServiceImpl service){
        this.notes=notes;this.redis=redis;this.service=service;
    }
    public static long score(Note note,LocalDateTime now){
        long weight=note.getViewCount()+note.getLikeCount()*5L+note.getCollectCount()*10L+note.getCommentCount()*3L;
        double hours=note.getPublishTime()==null?0:Math.max(0,Duration.between(note.getPublishTime(),now).toMinutes()/60.0);
        return Math.round(weight*100000.0/Math.pow(1+hours/24.0,1.5));
    }
    @Scheduled(fixedDelayString="${recommendation.refresh-ms:60000}",initialDelay=15000)
    public void refresh(){
        List<Note> candidates=notes.selectList(new LambdaQueryWrapper<Note>().eq(Note::getStatus,1));
        Set<ZSetOperations.TypedTuple<String>> scores=new HashSet<>();
        LocalDateTime now=LocalDateTime.now();
        for(Note note:candidates){
            long score=score(note,now);
            notes.update(null,new LambdaUpdateWrapper<Note>().eq(Note::getId,note.getId()).set(Note::getHotScore,score));
            scores.add(new DefaultTypedTuple<>(note.getId().toString(),(double)score));
        }
        try{
            String temp="recommend:hot:building:"+UUID.randomUUID();
            if(scores.isEmpty())redis.delete("recommend:hot");
            else {
                redis.opsForZSet().add(temp,scores);redis.expire(temp,120,TimeUnit.SECONDS);
                redis.rename(temp,"recommend:hot");
            }
            service.evictHotCache();
        }catch(Exception e){org.slf4j.LoggerFactory.getLogger(getClass()).warn("热榜Redis更新失败，使用数据库排序");}
    }
}

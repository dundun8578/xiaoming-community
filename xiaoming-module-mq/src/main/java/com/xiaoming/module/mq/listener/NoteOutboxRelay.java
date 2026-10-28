package com.xiaoming.module.mq.listener;
import com.xiaoming.module.mq.constant.MQConstant;
import com.xiaoming.module.note.entity.*;
import com.xiaoming.module.note.mapper.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
@Component
public class NoteOutboxRelay {
    private final NoteOutboxMapper outbox;
    private final NoteMapper notes;
    private final RabbitTemplate rabbit;
    public NoteOutboxRelay(NoteOutboxMapper outbox,NoteMapper notes,RabbitTemplate rabbit){
        this.outbox=outbox;this.notes=notes;this.rabbit=rabbit;
    }
    @Scheduled(fixedDelay=2000,initialDelay=5000)
    @Transactional(rollbackFor=Exception.class)
    public void deliver(){
        for(NoteOutbox row:outbox.lockPending()){
            try{
                String route=MQConstant.ROUTE_NOTE_UPDATE;
                Object payload=row.getNoteId();
                if("DELETE".equals(row.getEventType()))route=MQConstant.ROUTE_NOTE_DELETE;
                if("PUBLISH".equals(row.getEventType())){
                    Note note=notes.selectById(row.getNoteId());
                    if(note!=null && Integer.valueOf(1).equals(note.getStatus())){
                        route=MQConstant.ROUTE_NOTE_PUBLISH;payload=note;
                    }else route=MQConstant.ROUTE_NOTE_DELETE;
                }
                CorrelationData correlation=new CorrelationData(UUID.randomUUID().toString());
                rabbit.convertAndSend(MQConstant.NOTE_EXCHANGE,route,payload,correlation);
                if(!correlation.getFuture().get(5,TimeUnit.SECONDS).isAck() || correlation.getReturned()!=null)
                    throw new IllegalStateException("消息代理未确认");
                row.setSent(1);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();throw new IllegalStateException(e);
            }catch(Exception e){
                row.setAttempts(row.getAttempts()+1);
                row.setAvailableAt(LocalDateTime.now().plusSeconds(Math.min(60,1L<<Math.min(6,row.getAttempts()))));
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("消息暂未确认，保留外发表记录 id={}",row.getId());
            }
            outbox.updateById(row);
        }
    }
}

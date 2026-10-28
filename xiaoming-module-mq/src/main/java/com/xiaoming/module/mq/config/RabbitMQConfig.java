package com.xiaoming.module.mq.config;
import com.xiaoming.module.mq.constant.MQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {
    @Bean public DirectExchange noteExchange(){return new DirectExchange(MQConstant.NOTE_EXCHANGE,true,false);}
    private Queue durable(String name){return QueueBuilder.durable(name)
        .deadLetterExchange(MQConstant.DEAD_EXCHANGE).deadLetterRoutingKey("dead").build();}
    @Bean public Queue notePublishQueue(){return durable(MQConstant.NOTE_PUBLISH_QUEUE);}
    @Bean public Queue noteDeleteQueue(){return durable(MQConstant.NOTE_DELETE_QUEUE);}
    @Bean public Queue noteUpdateQueue(){return durable(MQConstant.NOTE_UPDATE_QUEUE);}
    @Bean public DirectExchange deadExchange(){return new DirectExchange(MQConstant.DEAD_EXCHANGE,true,false);}
    @Bean public Queue deadQueue(){return QueueBuilder.durable(MQConstant.DEAD_QUEUE).build();}
    @Bean public Declarables noteBindings(){
        return new Declarables(
            new Binding(MQConstant.NOTE_PUBLISH_QUEUE,Binding.DestinationType.QUEUE,MQConstant.NOTE_EXCHANGE,MQConstant.ROUTE_NOTE_PUBLISH,null),
            new Binding(MQConstant.NOTE_DELETE_QUEUE,Binding.DestinationType.QUEUE,MQConstant.NOTE_EXCHANGE,MQConstant.ROUTE_NOTE_DELETE,null),
            new Binding(MQConstant.NOTE_UPDATE_QUEUE,Binding.DestinationType.QUEUE,MQConstant.NOTE_EXCHANGE,MQConstant.ROUTE_NOTE_UPDATE,null),
            new Binding(MQConstant.DEAD_QUEUE,Binding.DestinationType.QUEUE,MQConstant.DEAD_EXCHANGE,"dead",null));
    }
    @Bean public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory,RabbitTemplateConfigurer configurer,Jackson2JsonMessageConverter converter){
        RabbitTemplate template=new RabbitTemplate();
        configurer.configure(template,connectionFactory);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        template.setConfirmCallback((correlation,ack,cause)->{
            if(!ack)System.err.println("MQ 投递未确认: "+cause);
        });
        template.setReturnsCallback(returned->System.err.println("MQ 消息未路由: "+returned.getReplyText()));
        return template;
    }
    @Bean public Jackson2JsonMessageConverter jackson2JsonMessageConverter(com.fasterxml.jackson.databind.ObjectMapper mapper){
        return new Jackson2JsonMessageConverter(mapper);
    }
}

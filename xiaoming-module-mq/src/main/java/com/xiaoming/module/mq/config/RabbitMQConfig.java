package com.xiaoming.module.mq.config;

import com.xiaoming.module.mq.constant.MQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange noteExchange() {
        return new DirectExchange(MQConstant.NOTE_EXCHANGE, true, false);
    }

    @Bean
    public Queue notePublishQueue() {
        return QueueBuilder.durable(MQConstant.NOTE_PUBLISH_QUEUE).build();
    }

    @Bean
    public Queue noteDeleteQueue() {
        return QueueBuilder.durable(MQConstant.NOTE_DELETE_QUEUE).build();
    }

    @Bean
    public Binding notePublishBinding(Queue notePublishQueue, DirectExchange noteExchange) {
        return BindingBuilder.bind(notePublishQueue)
                           .to(noteExchange)
                           .with(MQConstant.ROUTE_NOTE_PUBLISH);
    }

    @Bean
    public Binding noteDeleteBinding(Queue noteDeleteQueue, DirectExchange noteExchange) {
        return BindingBuilder.bind(noteDeleteQueue)
                           .to(noteExchange)
                           .with(MQConstant.ROUTE_NOTE_DELETE);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("消息发送失败: " + cause);
            }
        });
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

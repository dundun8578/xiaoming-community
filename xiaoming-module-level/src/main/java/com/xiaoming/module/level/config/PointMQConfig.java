package com.xiaoming.module.level.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

/**
 * 积分事件MQ配置
 * 定义积分相关的交换机、队列和路由
 */
@Configuration
public class PointMQConfig {

    /**
     * 积分事件交换机
     */
    public static final String POINT_EXCHANGE = "point.exchange";

    /**
     * 笔记发布队列
     */
    public static final String POINT_NOTE_PUBLISH_QUEUE = "point.note.publish.queue";

    /**
     * 评论发布队列
     */
    public static final String POINT_COMMENT_PUBLISH_QUEUE = "point.comment.publish.queue";

    /**
     * 点赞接收队列
     */
    public static final String POINT_LIKE_RECEIVED_QUEUE = "point.like.received.queue";

    /**
     * 收藏接收队列
     */
    public static final String POINT_COLLECT_RECEIVED_QUEUE = "point.collect.received.queue";

    /**
     * 关注接收队列
     */
    public static final String POINT_FOLLOW_RECEIVED_QUEUE = "point.follow.received.queue";

    /**
     * 主动关注队列
     */
    public static final String POINT_FOLLOW_OTHER_QUEUE = "point.follow.other.queue";

    @Bean
    public DirectExchange pointExchange() {
        return ExchangeBuilder.directExchange(POINT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue pointNotePublishQueue() {
        return QueueBuilder.durable(POINT_NOTE_PUBLISH_QUEUE).build();
    }

    @Bean
    public Queue pointCommentPublishQueue() {
        return QueueBuilder.durable(POINT_COMMENT_PUBLISH_QUEUE).build();
    }

    @Bean
    public Queue pointLikeReceivedQueue() {
        return QueueBuilder.durable(POINT_LIKE_RECEIVED_QUEUE).build();
    }

    @Bean
    public Queue pointCollectReceivedQueue() {
        return QueueBuilder.durable(POINT_COLLECT_RECEIVED_QUEUE).build();
    }

    @Bean
    public Queue pointFollowReceivedQueue() {
        return QueueBuilder.durable(POINT_FOLLOW_RECEIVED_QUEUE).build();
    }

    @Bean
    public Queue pointFollowOtherQueue() {
        return QueueBuilder.durable(POINT_FOLLOW_OTHER_QUEUE).build();
    }

    @Bean
    public Binding pointNotePublishBinding(DirectExchange pointExchange, Queue pointNotePublishQueue) {
        return BindingBuilder.bind(pointNotePublishQueue)
                .to(pointExchange)
                .with("point.note.publish");
    }

    @Bean
    public Binding pointCommentPublishBinding(DirectExchange pointExchange, Queue pointCommentPublishQueue) {
        return BindingBuilder.bind(pointCommentPublishQueue)
                .to(pointExchange)
                .with("point.comment.publish");
    }

    @Bean
    public Binding pointLikeReceivedBinding(DirectExchange pointExchange, Queue pointLikeReceivedQueue) {
        return BindingBuilder.bind(pointLikeReceivedQueue)
                .to(pointExchange)
                .with("point.like.received");
    }

    @Bean
    public Binding pointCollectReceivedBinding(DirectExchange pointExchange, Queue pointCollectReceivedQueue) {
        return BindingBuilder.bind(pointCollectReceivedQueue)
                .to(pointExchange)
                .with("point.collect.received");
    }

    @Bean
    public Binding pointFollowReceivedBinding(DirectExchange pointExchange, Queue pointFollowReceivedQueue) {
        return BindingBuilder.bind(pointFollowReceivedQueue)
                .to(pointExchange)
                .with("point.follow.received");
    }

    @Bean
    public Binding pointFollowOtherBinding(DirectExchange pointExchange, Queue pointFollowOtherQueue) {
        return BindingBuilder.bind(pointFollowOtherQueue)
                .to(pointExchange)
                .with("point.follow.other");
    }
}

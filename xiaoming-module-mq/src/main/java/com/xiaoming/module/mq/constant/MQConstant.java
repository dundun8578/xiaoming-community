package com.xiaoming.module.mq.constant;

/**
 * RabbitMQ常量
 */
public class MQConstant {

    /**
     * 笔记交换机
     */
    public static final String NOTE_EXCHANGE = "note.exchange";

    /**
     * 笔记发布队列
     */
    public static final String NOTE_PUBLISH_QUEUE = "note.publish.queue";

    /**
     * 笔记删除队列
     */
    public static final String NOTE_DELETE_QUEUE = "note.delete.queue";

    /**
     * 路由键 - 笔记发布
     */
    public static final String ROUTE_NOTE_PUBLISH = "note.publish";

    /**
     * 路由键 - 笔记删除
     */
    public static final String ROUTE_NOTE_DELETE = "note.delete";
}

package com.xiaoming.module.mq.constant;

/**
 * RabbitMQ常量
 */
public class MQConstant {
    public static final String NOTE_UPDATE_QUEUE="note.update.queue";
    public static final String ROUTE_NOTE_UPDATE="note.update";
    public static final String DEAD_EXCHANGE="note.dead.exchange";
    public static final String DEAD_QUEUE="note.dead.queue";

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

package com.xiaoming.module.message.vo;

import io.swagger.v3.annotations.ApiModel;
import io.swagger.v3.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("消息VO")
@Data
public class MessageVO {

    @ApiModelProperty("消息ID")
    private Long id;

    @ApiModelProperty("发送用户ID")
    private Long fromUserId;

    @ApiModelProperty("发送用户名称")
    private String fromUserName;

    @ApiModelProperty("接收用户ID")
    private Long toUserId;

    @ApiModelProperty("消息类型")
    private Integer type;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("关联目标ID")
    private Long targetId;

    @ApiModelProperty("是否已读")
    private Integer isRead;

    @ApiModelProperty("创建时间")
    private String createTime;
}

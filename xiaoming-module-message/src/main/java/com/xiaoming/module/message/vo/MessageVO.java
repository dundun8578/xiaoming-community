package com.xiaoming.module.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "消息VO")
@Data
public class MessageVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "发送用户ID")
    private Long fromUserId;

    @Schema(description = "发送用户名称")
    private String fromUserName;

    @Schema(description = "接收用户ID")
    private Long toUserId;

    @Schema(description = "消息类型")
    private Integer type;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "关联目标ID")
    private Long targetId;

    @Schema(description = "是否已读")
    private Integer isRead;

    @Schema(description = "创建时间")
    private String createTime;
}

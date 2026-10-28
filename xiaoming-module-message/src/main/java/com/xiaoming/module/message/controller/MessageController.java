package com.xiaoming.module.message.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.message.vo.MessageVO;
import com.xiaoming.module.message.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/message")
@Tag(name = "消息模块")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/list")
    public Result<List<MessageVO>> getMessages(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(messageService.getMessages(userId, pageNum, pageSize));
    }

    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(@RequestAttribute("userId") Long userId) {
        return Result.ok(messageService.getUnreadCount(userId));
    }

    @PutMapping("/{msgId}/read")
    public Result<Void> markRead(@RequestAttribute("userId") Long userId, @PathVariable Long msgId) {
        messageService.markRead(userId,msgId);
        return Result.ok();
    }
}

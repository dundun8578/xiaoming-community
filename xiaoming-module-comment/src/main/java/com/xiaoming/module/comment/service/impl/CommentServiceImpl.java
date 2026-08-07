package com.xiaoming.module.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoming.module.comment.entity.Comment;
import com.xiaoming.module.comment.mapper.CommentMapper;
import com.xiaoming.module.comment.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {}

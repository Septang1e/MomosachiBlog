package com.septangle.momosachiblog.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.septangle.momosachiblog.domain.entity.Comment;
import com.septangle.momosachiblog.mapper.CommentMapper;
import com.septangle.momosachiblog.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}

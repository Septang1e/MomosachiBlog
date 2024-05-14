package com.septangle.momosachiblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.septangle.momosachiblog.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}

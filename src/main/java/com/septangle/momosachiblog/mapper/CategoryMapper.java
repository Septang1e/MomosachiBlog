package com.septangle.momosachiblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.septangle.momosachiblog.domain.entity.Category;

public interface CategoryMapper extends BaseMapper<Category> {
    Long articleCount(Long categoryId);
    Category getByPid(String pid);
}

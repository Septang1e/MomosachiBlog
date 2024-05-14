package com.septangle.momosachiblog.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.septangle.momosachiblog.domain.entity.Category;
import com.septangle.momosachiblog.mapper.CategoryMapper;
import com.septangle.momosachiblog.service.ArticleService;
import com.septangle.momosachiblog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    private final CategoryMapper categoryMapper;
    @Autowired
    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Long articleCount(Long categoryId) {
        return categoryMapper.articleCount(categoryId);
    }
}

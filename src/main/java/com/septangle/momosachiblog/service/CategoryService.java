package com.septangle.momosachiblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.septangle.momosachiblog.domain.entity.Category;
import org.springframework.stereotype.Service;

@Service
public interface CategoryService extends IService<Category> {
    Long articleCount(Long categoryId);
}

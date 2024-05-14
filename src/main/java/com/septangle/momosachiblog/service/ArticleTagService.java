package com.septangle.momosachiblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.septangle.momosachiblog.domain.entity.ArticleTag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ArticleTagService extends IService<ArticleTag> {
    void updateArticleTag(Long article_id, List<Long> tag_id);
}

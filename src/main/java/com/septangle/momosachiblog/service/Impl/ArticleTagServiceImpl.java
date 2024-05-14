package com.septangle.momosachiblog.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.septangle.momosachiblog.domain.entity.ArticleTag;
import com.septangle.momosachiblog.mapper.ArticleTagMapper;
import com.septangle.momosachiblog.service.ArticleTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {

    private final ArticleTagMapper articleTagMapper;

    @Autowired
    public ArticleTagServiceImpl(ArticleTagMapper articleTagMapper) {
        this.articleTagMapper = articleTagMapper;
    }

    @Override
    public void updateArticleTag(Long article_id, List<Long> tag_id) {

        log.info("tagPidList: {}", tag_id);

        articleTagMapper.deleteOldTags(article_id);
        for(Long id : tag_id) {
            articleTagMapper.addNewTags(article_id, id);
        }
    }
}

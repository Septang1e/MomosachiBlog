package com.septangle.momosachiblog.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.septangle.momosachiblog.domain.entity.Article;
import com.septangle.momosachiblog.mapper.ArticleMapper;
import com.septangle.momosachiblog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleMapper articleMapper;

    @Autowired
    public ArticleServiceImpl(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @Override
    public List<Article> getArticleWithTagId(Long tagId) {
        return articleMapper.getArticleWithTagId(tagId);
    }
    @Override
    public Article getByPid(String pid){
        return articleMapper.getByPid(pid);
    }
    @Override
    public List<Article> getAllByTagPid(String pid) {
        return articleMapper.getAllByTagPid(pid);
    }

    @Override
    public List<Article> getByCategoryPid(String pid) {
        return articleMapper.getByCategoryPid(pid);
    }
}
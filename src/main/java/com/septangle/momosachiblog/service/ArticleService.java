package com.septangle.momosachiblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.septangle.momosachiblog.domain.entity.Article;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ArticleService extends IService<Article> {
    List<Article> getArticleWithTagId(Long tagId);
    Article getByPid(String pid);
    List<Article> getAllByTagPid(String pid);
    List<Article> getByCategoryPid(String pid);
}

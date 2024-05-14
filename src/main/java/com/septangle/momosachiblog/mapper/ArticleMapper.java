package com.septangle.momosachiblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.septangle.momosachiblog.domain.entity.Article;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    List<Article> getArticleWithTagId(Long tagId);

    Article getByPid(String pid);
    List<Article> getAllByTagPid(String pid);
    List<Article> getByCategoryPid(String pid);

}

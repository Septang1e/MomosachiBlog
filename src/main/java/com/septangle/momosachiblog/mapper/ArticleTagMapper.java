package com.septangle.momosachiblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.septangle.momosachiblog.domain.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
    void deleteOldTags(Long article_id);
    void addNewTags(Long article_id, Long tag_id);
}

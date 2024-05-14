package com.septangle.momosachiblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.septangle.momosachiblog.domain.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    public List<Tag> getTagsWithArticleId(Long articleId);

    public Long articleCount(Long tagId);

    public Tag getByPid(String pid);

    public List<Tag> getAllByArticlePid(String pid);

    @Select("select * from septangle_blog.tag where name = #{name} limit 1")
    public Tag getByName(String name);
}

package com.septangle.momosachiblog.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.septangle.momosachiblog.domain.entity.Tag;
import com.septangle.momosachiblog.domain.repository.TagRepository;
import com.septangle.momosachiblog.mapper.TagMapper;
import com.septangle.momosachiblog.module.es.TagModule;
import com.septangle.momosachiblog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Date;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final TagMapper tagMapper;


    @Autowired
    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }


    @Override
    public List<Tag> getTagsWithArticleId(Long articleId) {
        return tagMapper.getTagsWithArticleId(articleId);
    }

    @Override
    public Long articleCount(Long tagId) {
        return tagMapper.articleCount(tagId);
    }

    @Override
    public Tag getByPid(String pid){
        return tagMapper.getByPid(pid);
    }

    @Override
    public List<Tag> getAllByArticlePid(String pid) {
        return tagMapper.getAllByArticlePid(pid);
    }

    @Override
    public Tag getByName(String name) {
        return tagMapper.getByName(name);
    }


}

package com.septangle.momosachiblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.septangle.momosachiblog.domain.entity.Tag;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface TagService extends IService<Tag> {
    public List<Tag> getTagsWithArticleId(Long articleId);
    public Long articleCount(Long tagId);
    public Tag getByPid(String pid);
    public List<Tag> getAllByArticlePid(String pid);

    public Tag getByName(String name);

}

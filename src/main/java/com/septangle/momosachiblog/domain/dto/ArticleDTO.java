package com.septangle.momosachiblog.domain.dto;


import com.septangle.momosachiblog.domain.entity.Article;
import com.septangle.momosachiblog.domain.entity.Tag;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ArticleDTO {

    private String title;

    private String content;

    private List<TagDTO> tags;

    private String pid;

    private String category;

    private String categoryPid;

    private String description;

    private String thumbnail;

    private Integer isComment;

    private Integer viewCount;

    private Integer likeCount;

    private Date createTime;

    private Date updateTime;

    private Long updateUser;

    private Long createUser;
}

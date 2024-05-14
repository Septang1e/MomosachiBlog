package com.septangle.momosachiblog.domain.dto;

import com.septangle.momosachiblog.domain.entity.Tag;
import lombok.Data;

import java.util.List;

@Data
public class ArticleUploadDTO {

    private Long id;

    private String title;

    private String content;

    private List<Tag> tags;

    private String pid;

    private String thumbnail;

    private Integer isComment;

    private Integer status;// 1 草稿，0默认

    private Integer isDelete;

    private Long categoryId;

    private String category;

    private String description;
}

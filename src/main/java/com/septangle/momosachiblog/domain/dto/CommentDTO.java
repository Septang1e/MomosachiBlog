package com.septangle.momosachiblog.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CommentDTO {

    private Long floor;

    private Long likeCount;

    private Long commentId;

    private Long rootId;

    private String ipAddress;

    private String articlePid;

    private String avatar;

    private String content;

    private Date createTime;

    private String nickname;

    private String toName;

    private Long toId;

    private String avatarRandom;

    //用于检测重复的username和email是否相匹配
    private String email;

    private String website;
}

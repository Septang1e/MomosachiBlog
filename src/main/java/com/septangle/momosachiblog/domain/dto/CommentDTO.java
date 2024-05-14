package com.septangle.momosachiblog.domain.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CommentDTO {

    // -1 的时候没有father
    private Long fatherId;

    private Long floor;

    private Long likeCount;

    private Long rootParentId;

    private String ipAddress;

    private Long commentId;

    private Long replyToFeature;

    private Long articleId;

    private String pid;

    private String avatar;

    private String content;

    private Date createTime;

    private String nickname;

    private String replyTo;

    private String avatarRandom;

    //用于检测重复的username和email是否相匹配
    private String email;

    private String website;
}

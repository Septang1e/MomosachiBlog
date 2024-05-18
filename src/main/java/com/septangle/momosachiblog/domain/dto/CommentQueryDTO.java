package com.septangle.momosachiblog.domain.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentQueryDTO {

    private UserQueryDTO user;

    private Long commentId;

    private String articleTitle;

    private String content;

    private Integer status;

}

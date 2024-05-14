package com.septangle.momosachiblog.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommentTransDTO {

    private CommentDTO root;

    private List<CommentDTO>children;
}

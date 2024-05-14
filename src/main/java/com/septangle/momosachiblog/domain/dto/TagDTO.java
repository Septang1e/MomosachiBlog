package com.septangle.momosachiblog.domain.dto;

import com.septangle.momosachiblog.domain.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {

    private Tag tag;

    private Long articleCount;
}
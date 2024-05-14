package com.septangle.momosachiblog.domain.dto;

import com.septangle.momosachiblog.domain.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    private Category category;

    private Long articleCount;
}
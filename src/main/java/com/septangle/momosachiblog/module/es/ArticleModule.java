package com.septangle.momosachiblog.module.es;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Document(indexName = "article")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class ArticleModule implements Serializable{
    @Id
    private String id;

    //@Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Keyword)
    private String pid;

}
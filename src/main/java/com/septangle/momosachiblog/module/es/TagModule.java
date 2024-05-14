package com.septangle.momosachiblog.module.es;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.cms.PasswordRecipientId;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Document(indexName = "tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagModule implements Serializable {

    @Id
    private String id;

    //,analyzer = "ik_smart",
    //            searchAnalyzer = "ik_smart")

    //@Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String pid;

}
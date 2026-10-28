package com.xiaoming.module.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.time.LocalDateTime;

/**
 * 笔记ES文档
 */
@Data
@Document(indexName = "note", createIndex = false)
public class NoteDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Keyword)
    private String tags;

    @Field(type = FieldType.Long)
    private Long userId;

    private String coverImage;

    @Field(type = FieldType.Text)
    private String authorName;

    @Field(type = FieldType.Integer)
    private Integer type;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Long)
    private Long likeCount;

    @Field(type = FieldType.Long)
    private Long collectCount;

    @Field(type = FieldType.Long)
    private Long commentCount;

    @Field(type = FieldType.Long)
    private Long hotScore;

    @Field(type = FieldType.Date)
    private LocalDateTime publishTime;
}

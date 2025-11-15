package com.blog.article.dto;

import lombok.Data;

@Data
public class ArticleCreateDTO {
    private String title;
    private String content;
    private String coverImage;
}
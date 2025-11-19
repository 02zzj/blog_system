package com.blog.article.dto;

import lombok.Data;

@Data
public class ArticleUpdateDTO {
    private String title;
    private String content;
    private String coverImage;
    private boolean removeCoverImage; // 标记是否需要删除封面图
}
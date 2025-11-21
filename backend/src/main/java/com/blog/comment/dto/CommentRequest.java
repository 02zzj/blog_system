package com.blog.comment.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long articleId;
    private String content;
}
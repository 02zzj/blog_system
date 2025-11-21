package com.blog.comment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private Long articleId;
    private Long userId;
    private String userNickname;
    private String content;
    private LocalDateTime createdAt;
    private Integer floorNumber; // 楼层号
}
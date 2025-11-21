package com.blog.comment.service;

import com.blog.comment.dto.CommentDTO;
import com.blog.comment.dto.CommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    // 创建评论
    CommentDTO createComment(CommentRequest commentRequest, Long userId);
    
    // 根据文章ID获取评论列表
    Page<CommentDTO> getCommentsByArticleId(Long articleId, Pageable pageable, String sortOrder);
    
    // 根据用户ID获取评论列表
    Page<CommentDTO> getCommentsByUserId(Long userId, Pageable pageable);
    
    // 删除评论（检查权限）
    void deleteComment(Long commentId, Long userId);
    
    // 检查用户是否有权限删除评论
    boolean hasPermissionToDelete(Long commentId, Long userId);
    
    // 获取文章的评论数量
    long countCommentsByArticleId(Long articleId);
}
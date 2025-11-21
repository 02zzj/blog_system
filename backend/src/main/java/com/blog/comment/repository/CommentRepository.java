package com.blog.comment.repository;

import com.blog.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 根据文章ID查询评论列表，按创建时间倒序排列
    Page<Comment> findByArticleIdOrderByCreatedAtDesc(Long articleId, Pageable pageable);
    
    // 根据文章ID查询评论列表，按创建时间升序排列
    Page<Comment> findByArticleIdOrderByCreatedAtAsc(Long articleId, Pageable pageable);
    
    // 根据文章ID统计评论数量
    long countByArticleId(Long articleId);
    
    // 根据用户ID查询评论列表
    Page<Comment> findByUserId(Long userId, Pageable pageable);
}
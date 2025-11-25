package com.blog.comment.repository;

import com.blog.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // 查询所有评论，按创建时间倒序排列（用于管理员）
    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 根据内容搜索评论
    Page<Comment> findByContentContainingOrderByCreatedAtDesc(String keyword, Pageable pageable);
    
    // 根据用户昵称搜索评论
    @Query("SELECT c FROM Comment c WHERE c.user.nickname LIKE %:keyword% ORDER BY c.createdAt DESC")
    Page<Comment> findByUserNicknameContainingOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);
    
    // 搜索评论内容或用户昵称
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% OR c.user.nickname LIKE %:keyword% ORDER BY c.createdAt DESC")
    Page<Comment> findByContentOrUserNicknameContainingOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);
}
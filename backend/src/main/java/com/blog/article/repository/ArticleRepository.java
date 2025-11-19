package com.blog.article.repository;

import com.blog.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findAll(Pageable pageable);
    
    // 根据作者ID查询文章列表
    Page<Article> findByAuthorId(Long authorId, Pageable pageable);
    
    // 根据标题和内容进行模糊搜索
    @Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword% OR a.content LIKE %:keyword%")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 根据用户ID和关键词进行模糊搜索
    @Query("SELECT a FROM Article a WHERE a.author.id = :userId AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Article> searchByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
    
    // 根据作者昵称进行模糊搜索
    @Query("SELECT a FROM Article a WHERE a.author.nickname LIKE %:nickname%")
    Page<Article> searchByAuthorNickname(@Param("nickname") String nickname, Pageable pageable);
    
    // 根据关键词和作者昵称进行模糊搜索
    @Query("SELECT a FROM Article a WHERE (a.title LIKE %:keyword% OR a.content LIKE %:keyword% OR a.author.nickname LIKE %:keyword%)")
    Page<Article> searchByKeywordAndAuthorNickname(@Param("keyword") String keyword, Pageable pageable);
}
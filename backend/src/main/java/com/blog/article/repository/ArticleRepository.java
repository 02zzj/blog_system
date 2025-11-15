package com.blog.article.repository;

import com.blog.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findAll(Pageable pageable);
    
    // 根据作者ID查询文章列表
    Page<Article> findByAuthorId(Long authorId, Pageable pageable);
}
package com.blog.article.service;

import com.blog.article.dto.ArticleCreateDTO;
import com.blog.article.entity.Article;
import com.blog.user.entity.User;
import org.springframework.data.domain.Page;

public interface ArticleService {
    Article createArticle(ArticleCreateDTO dto, User author);
    Page<Article> getArticles(int page, int size, String sortField, String sortDirection);
    Page<Article> getArticlesByUserId(Long userId, int page, int size, String sortField, String sortDirection);
    Article getArticle(Long id);
    void deleteArticle(Long id, User operator);
}
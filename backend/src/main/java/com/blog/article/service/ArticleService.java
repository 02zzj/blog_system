package com.blog.article.service;

import com.blog.article.dto.ArticleCreateDTO;
import com.blog.article.entity.Article;
import com.blog.user.entity.User;
import org.springframework.data.domain.Page;

public interface ArticleService {
    Article createArticle(ArticleCreateDTO dto, User author);
    Page<Article> getArticles(int page, int size, String sortField, String sortDirection);
    Page<Article> getArticlesByUserId(Long userId, int page, int size, String sortField, String sortDirection);
    // 根据关键词搜索文章
    Page<Article> searchArticles(String keyword, int page, int size, String sortField, String sortDirection);
    // 根据用户ID和关键词搜索文章
    Page<Article> searchArticlesByUserId(Long userId, String keyword, int page, int size, String sortField, String sortDirection);
    // 根据作者昵称搜索文章
    Page<Article> searchArticlesByAuthorNickname(String nickname, int page, int size, String sortField, String sortDirection);
    // 根据关键词搜索文章（包括标题、内容和作者昵称）
    Page<Article> searchArticlesWithAuthor(String keyword, int page, int size, String sortField, String sortDirection);
    Article getArticle(Long id);
    void deleteArticle(Long id, User operator);
}
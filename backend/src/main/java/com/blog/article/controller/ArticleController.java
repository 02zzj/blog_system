package com.blog.article.controller;

import com.blog.article.dto.ArticleCreateDTO;
import com.blog.article.dto.ArticleUpdateDTO;
import com.blog.article.service.ArticleService;
import com.blog.common.ResponseResult;
import com.blog.user.entity.User;
import com.blog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseResult<Object> create(@RequestBody ArticleCreateDTO dto) {
        User currentUser = userService.getCurrentUser();
        return ResponseResult.success(articleService.createArticle(dto, currentUser));
    }

    @GetMapping
    public ResponseResult<Page<?>> list(
            @RequestParam(defaultValue = "1") int page, 
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String authorNickname,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection) {
        Page<?> articles;
        
        // 优先处理按作者昵称搜索
        if (authorNickname != null && !authorNickname.trim().isEmpty()) {
            articles = articleService.searchArticlesByAuthorNickname(authorNickname, page, size, sortField, sortDirection);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 有搜索关键词的情况
            if (userId != null) {
                // 根据用户ID和关键词搜索
                articles = articleService.searchArticlesByUserId(userId, keyword, page, size, sortField, sortDirection);
            } else {
                // 根据searchType决定使用哪种搜索方式
                if ("title_content".equals(searchType)) {
                    // 仅搜索标题和内容
                    articles = articleService.searchArticles(keyword, page, size, sortField, sortDirection);
                } else {
                    // 默认搜索标题、内容和作者昵称
                    articles = articleService.searchArticlesWithAuthor(keyword, page, size, sortField, sortDirection);
                }
            }
        } else {
            // 没有搜索关键词的情况
            if (userId != null) {
                articles = articleService.getArticlesByUserId(userId, page, size, sortField, sortDirection);
            } else {
                articles = articleService.getArticles(page, size, sortField, sortDirection);
            }
        }
        
        return ResponseResult.success(articles);
    }

    @GetMapping("/{id}")
    public ResponseResult<Object> detail(@PathVariable Long id) {
        return ResponseResult.success(articleService.getArticle(id));
    }

    @PutMapping("/{id}")
    public ResponseResult<Object> update(@PathVariable Long id, @RequestBody ArticleUpdateDTO dto) {
        User currentUser = userService.getCurrentUser();
        return ResponseResult.success(articleService.updateArticle(id, dto, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        articleService.deleteArticle(id, currentUser);
        return ResponseResult.success(null);
    }
}
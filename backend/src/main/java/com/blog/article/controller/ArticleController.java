package com.blog.article.controller;

import com.blog.article.dto.ArticleCreateDTO;
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
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection) {
        Page<?> articles;
        if (userId != null) {
            articles = articleService.getArticlesByUserId(userId, page, size, sortField, sortDirection);
        } else {
            articles = articleService.getArticles(page, size, sortField, sortDirection);
        }
        return ResponseResult.success(articles);
    }

    @GetMapping("/{id}")
    public ResponseResult<Object> detail(@PathVariable Long id) {
        return ResponseResult.success(articleService.getArticle(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        articleService.deleteArticle(id, currentUser);
        return ResponseResult.success(null);
    }
}
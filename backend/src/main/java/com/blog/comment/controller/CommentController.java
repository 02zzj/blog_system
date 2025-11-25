package com.blog.comment.controller;

import com.blog.comment.dto.CommentRequest;
import com.blog.comment.service.CommentService;
import com.blog.common.ResponseResult;
import com.blog.user.entity.User;
import com.blog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    // 创建评论
    @PostMapping
    public ResponseResult<Object> create(@Validated @RequestBody CommentRequest request) {
        User currentUser = userService.getCurrentUser();
        return ResponseResult.success(commentService.createComment(request, currentUser.getId()));
    }

    // 获取文章的评论列表
    @GetMapping("/article/{articleId}")
    public ResponseResult<Page<?>> getCommentsByArticleId(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        // 创建Pageable对象，注意Spring Data JPA页码从0开始
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseResult.success(commentService.getCommentsByArticleId(articleId, pageable, sortOrder));
    }
    
    // 获取文章的评论数量
    @GetMapping("/article/{articleId}/count")
    public ResponseResult<Long> getCommentCountByArticleId(
            @PathVariable Long articleId) {
        return ResponseResult.success(commentService.countCommentsByArticleId(articleId));
    }

    // 获取用户的评论列表
    @GetMapping("/user/{userId}")
    public ResponseResult<Page<?>> getCommentsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // 创建Pageable对象，注意Spring Data JPA页码从0开始
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseResult.success(commentService.getCommentsByUserId(userId, pageable));
    }

    // 获取所有评论（管理员功能）
    @GetMapping
    public ResponseResult<Page<?>> getAllComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String searchType) {
        // 创建Pageable对象，注意Spring Data JPA页码从0开始
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 如果有关键词，则执行搜索，否则获取所有评论
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseResult.success(commentService.searchComments(keyword.trim(), searchType, pageable));
        } else {
            return ResponseResult.success(commentService.getAllComments(pageable));
        }
    }

    // 删除评论
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        commentService.deleteComment(id, currentUser.getId());
        return ResponseResult.success(null);
    }
}
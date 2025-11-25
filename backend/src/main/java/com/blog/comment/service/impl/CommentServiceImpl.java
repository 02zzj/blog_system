package com.blog.comment.service.impl;

import com.blog.article.entity.Article;
import com.blog.article.repository.ArticleRepository;
import com.blog.comment.dto.CommentDTO;
import com.blog.comment.dto.CommentRequest;
import com.blog.comment.entity.Comment;
import com.blog.comment.repository.CommentRepository;
import com.blog.comment.service.CommentService;
import com.blog.user.entity.User;
import com.blog.user.repository.UserRepository;
import com.blog.user.util.UserRoleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public CommentDTO createComment(CommentRequest request, Long userId) {
        // 验证文章是否存在
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new NoSuchElementException("文章不存在"));
                
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 创建评论
        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setUser(user);
        comment.setContent(request.getContent());
        
        // 保存评论
        comment = commentRepository.save(comment);
        
        // 转换为DTO
        return convertToDTO(comment);
    }

    @Override
    public Page<CommentDTO> getCommentsByArticleId(Long articleId, Pageable pageable, String sortOrder) {
        // 验证文章是否存在
        articleRepository.findById(articleId)
                .orElseThrow(() -> new NoSuchElementException("文章不存在"));
        
        // 获取总评论数
        long totalComments = commentRepository.countByArticleId(articleId);
        
        // 根据排序参数选择不同的查询方法
        Page<Comment> comments;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            // 按创建时间升序排列（最早的评论在前）
            comments = commentRepository.findByArticleIdOrderByCreatedAtAsc(articleId, pageable);
        } else {
            // 默认按创建时间降序排列（最新的评论在前）
            comments = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId, pageable);
        }
        
        // 转换为DTO并设置楼层号
        return comments.map(comment -> {
            CommentDTO dto = convertToDTO(comment);
            int pageStartIndex = pageable.getPageNumber() * pageable.getPageSize();
            
            if ("asc".equalsIgnoreCase(sortOrder)) {
                // 升序排列时，楼层号从1开始递增
                // 第0页第0条评论是1楼，第0页第1条评论是2楼，以此类推
                dto.setFloorNumber(pageStartIndex + 1);
            } else {
                // 降序排列时，楼层号从总数开始递减
                // 第0页第0条评论是总评论数楼，第0页第1条评论是总评论数-1楼，以此类推
                dto.setFloorNumber((int)(totalComments - pageStartIndex));
            }
            
            return dto;
        });
    }

    @Override
    public Page<CommentDTO> getCommentsByUserId(Long userId, Pageable pageable) {
        // 验证用户是否存在
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 查询评论列表
        Page<Comment> comments = commentRepository.findByUserId(userId, pageable);
        
        // 转换为DTO
        return comments.map(this::convertToDTO);
    }

    @Override
    public Page<CommentDTO> getAllComments(Pageable pageable) {
        // 查询所有评论，按创建时间倒序排列
        Page<Comment> comments = commentRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        // 转换为DTO
        return comments.map(this::convertToDTO);
    }
    
    @Override
    public Page<CommentDTO> searchComments(String keyword, String searchType, Pageable pageable) {
        Page<Comment> comments;
        
        // 根据搜索类型选择不同的查询方法
        switch (searchType) {
            case "content":
                // 仅搜索评论内容
                comments = commentRepository.findByContentContainingOrderByCreatedAtDesc(keyword, pageable);
                break;
            case "author":
                // 仅搜索评论者昵称
                comments = commentRepository.findByUserNicknameContainingOrderByCreatedAtDesc(keyword, pageable);
                break;
            case "all":
            default:
                // 搜索评论内容和评论者昵称
                comments = commentRepository.findByContentOrUserNicknameContainingOrderByCreatedAtDesc(keyword, pageable);
                break;
        }
        
        // 转换为DTO
        return comments.map(this::convertToDTO);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        // 查询评论
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("评论不存在"));
        
        // 查询用户信息，用于检查是否为管理员
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 验证权限（管理员或评论作者或文章作者可以删除评论）
        boolean isAdmin = UserRoleUtil.isAdmin(user);
        boolean isCommentAuthor = comment.getUser().getId().equals(userId);
        boolean isArticleAuthor = comment.getArticle().getAuthor().getId().equals(userId);
        
        if (!isAdmin && !isCommentAuthor && !isArticleAuthor) {
            throw new RuntimeException("无权删除此评论");
        }
        
        // 删除评论
        commentRepository.delete(comment);
    }

    @Override
    public boolean hasPermissionToDelete(Long commentId, Long userId) {
        // 查询评论
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("评论不存在"));
        
        // 查询用户信息，用于检查是否为管理员
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 验证权限（管理员或评论作者或文章作者可以删除评论）
        boolean isAdmin = UserRoleUtil.isAdmin(user);
        boolean isCommentAuthor = comment.getUser().getId().equals(userId);
        boolean isArticleAuthor = comment.getArticle().getAuthor().getId().equals(userId);
        
        return isAdmin || isCommentAuthor || isArticleAuthor;
    }
    
    @Override
    public long countCommentsByArticleId(Long articleId) {
        // 验证文章是否存在
        articleRepository.findById(articleId)
                .orElseThrow(() -> new NoSuchElementException("文章不存在"));
        
        // 统计评论数量
        return commentRepository.countByArticleId(articleId);
    }
    
    // 将实体转换为DTO
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setArticleId(comment.getArticle().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserNickname(comment.getUser().getNickname() != null ? comment.getUser().getNickname() : comment.getUser().getEmail());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
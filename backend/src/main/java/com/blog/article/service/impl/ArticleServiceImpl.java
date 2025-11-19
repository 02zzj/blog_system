package com.blog.article.service.impl;

import com.blog.article.dto.ArticleCreateDTO;
import com.blog.article.dto.ArticleUpdateDTO;
import com.blog.article.entity.Article;
import com.blog.article.repository.ArticleRepository;
import com.blog.article.service.ArticleService;
import com.blog.file.util.FileUtils;
import com.blog.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private FileUtils fileUtils;

    @Override
    public Article createArticle(ArticleCreateDTO dto, User author) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setCoverImage(dto.getCoverImage());
        article.setAuthor(author);
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());

        return articleRepository.save(article);
    }

    @Override
    public Article updateArticle(Long id, ArticleUpdateDTO dto, User operator) {
        Article article = getArticle(id);
        
        // 验证是否是作者本人
        if (!article.getAuthor().getId().equals(operator.getId())) {
            throw new RuntimeException("无权修改此文章");
        }
        
        // 保存旧的封面图路径
        String oldCoverImage = article.getCoverImage();
        
        // 更新文章信息
        if (dto.getTitle() != null) {
            article.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            // 如果内容改变，需要处理旧内容中的图片
            if (!article.getContent().equals(dto.getContent())) {
                // 清理旧内容中的图片
                List<String> imageUrls = fileUtils.extractImageUrlsFromHtml(article.getContent());
                for (String imageUrl : imageUrls) {
                    String filename = fileUtils.extractFilenameFromUrl(imageUrl);
                    if (filename != null) {
                        fileUtils.deleteFile(filename);
                    }
                }
                article.setContent(dto.getContent());
            }
        }
        
        // 处理封面图更新或删除
        if (dto.isRemoveCoverImage() && oldCoverImage != null && !oldCoverImage.isEmpty()) {
            // 删除旧封面图
            String oldCoverFilename = fileUtils.extractFilenameFromUrl(oldCoverImage);
            if (oldCoverFilename != null) {
                fileUtils.deleteFile(oldCoverFilename);
            }
            article.setCoverImage(null);
        } else if (dto.getCoverImage() != null) {
            // 更新封面图，删除旧的
            if (oldCoverImage != null && !oldCoverImage.isEmpty() && !oldCoverImage.equals(dto.getCoverImage())) {
                String oldCoverFilename = fileUtils.extractFilenameFromUrl(oldCoverImage);
                if (oldCoverFilename != null) {
                    fileUtils.deleteFile(oldCoverFilename);
                }
            }
            article.setCoverImage(dto.getCoverImage());
        }
        
        // 更新修改时间
        article.setUpdatedAt(LocalDateTime.now());
        
        return articleRepository.save(article);
    }

    @Override
    public Page<Article> getArticles(int page, int size, String sortField, String sortDirection) {
        // 注意：Spring Data JPA的页码从0开始
        // 默认为按updatedAt降序排序
        if (sortField == null || sortField.isEmpty()) {
            sortField = "updatedAt";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }
        
        // 创建排序对象
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
            sortDirection.equalsIgnoreCase("asc") ? 
            org.springframework.data.domain.Sort.Direction.ASC : 
            org.springframework.data.domain.Sort.Direction.DESC,
            sortField
        );
        
        return articleRepository.findAll(PageRequest.of(page - 1, size, sort));
    }
    
    @Override
    public Page<Article> getArticlesByUserId(Long userId, int page, int size, String sortField, String sortDirection) {
        // 注意：Spring Data JPA的页码从0开始
        // 默认为按updatedAt降序排序
        if (sortField == null || sortField.isEmpty()) {
            sortField = "updatedAt";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }
        
        // 创建排序对象
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
            sortDirection.equalsIgnoreCase("asc") ? 
            org.springframework.data.domain.Sort.Direction.ASC : 
            org.springframework.data.domain.Sort.Direction.DESC,
            sortField
        );
        
        return articleRepository.findByAuthorId(userId, PageRequest.of(page - 1, size, sort));
    }

    @Override
    public Article getArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
    }

    @Override
    public Page<Article> searchArticles(String keyword, int page, int size, String sortField, String sortDirection) {
        // 设置默认排序
        if (sortField == null || sortField.isEmpty()) {
            sortField = "updatedAt";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }
        
        // 创建排序对象
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
            sortDirection.equalsIgnoreCase("asc") ? 
            org.springframework.data.domain.Sort.Direction.ASC : 
            org.springframework.data.domain.Sort.Direction.DESC,
            sortField
        );
        
        return articleRepository.searchByKeyword(keyword, PageRequest.of(page - 1, size, sort));
    }
    
    @Override
    public Page<Article> searchArticlesByUserId(Long userId, String keyword, int page, int size, String sortField, String sortDirection) {
        // 设置默认排序
        if (sortField == null || sortField.isEmpty()) {
            sortField = "updatedAt";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }
        
        // 创建排序对象
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
            sortDirection.equalsIgnoreCase("asc") ? 
            org.springframework.data.domain.Sort.Direction.ASC : 
            org.springframework.data.domain.Sort.Direction.DESC,
            sortField
        );
        
        return articleRepository.searchByUserIdAndKeyword(userId, keyword, PageRequest.of(page - 1, size, sort));
    }
    
    @Override
    public Page<Article> searchArticlesByAuthorNickname(String nickname, int page, int size, String sortField, String sortDirection) {
        // 设置默认排序
        if (sortField == null || sortField.isEmpty()) {
            sortField = "updatedAt";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }
        
        // 创建排序对象
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
            sortDirection.equalsIgnoreCase("asc") ? 
            org.springframework.data.domain.Sort.Direction.ASC : 
            org.springframework.data.domain.Sort.Direction.DESC,
            sortField
        );
        
        return articleRepository.searchByAuthorNickname(nickname, PageRequest.of(page - 1, size, sort));
    }
    
    @Override
    public Page<Article> searchArticlesWithAuthor(String keyword, int page, int size, String sortField, String sortDirection) {
        // 设置默认排序
        if (sortField == null || sortField.isEmpty()) {
            sortField = "updatedAt";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }
        
        // 创建排序对象
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
            sortDirection.equalsIgnoreCase("asc") ? 
            org.springframework.data.domain.Sort.Direction.ASC : 
            org.springframework.data.domain.Sort.Direction.DESC,
            sortField
        );
        
        return articleRepository.searchByKeywordAndAuthorNickname(keyword, PageRequest.of(page - 1, size, sort));
    }
    
    @Override
    public void deleteArticle(Long id, User operator) {
        Article article = getArticle(id);
        
        // 验证是否是作者本人
        if (!article.getAuthor().getId().equals(operator.getId())) {
            throw new RuntimeException("无权删除此文章");
        }
        
        // 在删除文章前，先删除相关的图片文件
        String content = article.getContent();
        String coverImage = article.getCoverImage();
        fileUtils.deleteArticleImages(content, coverImage);
        
        articleRepository.delete(article);
    }
}
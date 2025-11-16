package com.blog.article.service.impl;

import com.blog.article.dto.ArticleCreateDTO;
import com.blog.article.entity.Article;
import com.blog.article.repository.ArticleRepository;
import com.blog.article.service.ArticleService;
import com.blog.file.util.FileUtils;
import com.blog.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

        return articleRepository.save(article);
    }

    @Override
    public Page<Article> getArticles(int page, int size) {
        // 注意：Spring Data JPA的页码从0开始
        return articleRepository.findAll(PageRequest.of(page - 1, size));
    }
    
    @Override
    public Page<Article> getArticlesByUserId(Long userId, int page, int size) {
        // 注意：Spring Data JPA的页码从0开始
        return articleRepository.findByAuthorId(userId, PageRequest.of(page - 1, size));
    }

    @Override
    public Article getArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
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
package com.blog.file.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

@Component
public class FileUtils {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    /**
     * 从HTML内容中提取图片URL
     */
    public List<String> extractImageUrlsFromHtml(String htmlContent) {
        List<String> imageUrls = new ArrayList<>();
        if (htmlContent == null || htmlContent.isEmpty()) {
            return imageUrls;
        }

        // 正则表达式匹配<img>标签中的src属性
        Pattern pattern = Pattern.compile("<img[^>]*src=[\"']?([^\"'>]+)[\"']?[^>]*>");
        Matcher matcher = pattern.matcher(htmlContent);

        while (matcher.find()) {
            String src = matcher.group(1);
            if (src != null && src.startsWith(baseUrl)) {
                imageUrls.add(src);
            }
        }

        return imageUrls;
    }

    /**
     * 从URL中提取文件名
     */
    public String extractFilenameFromUrl(String url) {
        if (url == null || !url.startsWith(baseUrl)) {
            return null;
        }

        // 从URL中提取文件名部分
        return url.substring(baseUrl.length() + 1);
    }

    /**
     * 删除指定文件
     */
    public boolean deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        try {
            Path filePath = Paths.get(uploadDir, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("文件已删除: " + filename);
                return true;
            } else {
                logger.warning("文件不存在: " + filename);
                return false;
            }
        } catch (IOException e) {
            logger.severe("删除文件失败: " + filename + ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除文章相关的所有图片
     */
    public void deleteArticleImages(String content, String coverImage) {
        // 删除封面图片
        if (coverImage != null && !coverImage.isEmpty()) {
            String coverFilename = extractFilenameFromUrl(coverImage);
            if (coverFilename != null) {
                deleteFile(coverFilename);
            }
        }

        // 删除内容中的图片
        List<String> imageUrls = extractImageUrlsFromHtml(content);
        for (String imageUrl : imageUrls) {
            String filename = extractFilenameFromUrl(imageUrl);
            if (filename != null) {
                deleteFile(filename);
            }
        }
    }
}
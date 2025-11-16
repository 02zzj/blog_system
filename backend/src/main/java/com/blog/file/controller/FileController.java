package com.blog.file.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return new ResponseEntity<>("请选择要上传的文件", HttpStatus.BAD_REQUEST);
            }

            // 检查文件类型，只允许图片
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return new ResponseEntity<>("只允许上传图片文件", HttpStatus.BAD_REQUEST);
            }

            // 确保上传目录存在
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(uploadDir, uniqueFilename);
            Files.write(filePath, file.getBytes());

            // 返回图片URL
            String fileUrl = baseUrl + "/" + uniqueFilename;
            return ResponseEntity.ok(new FileUploadResponse(fileUrl, uniqueFilename));

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 删除指定的图片文件
     * 用于清理未使用的临时图片
     */
    @DeleteMapping("/delete/{filename}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFile(@PathVariable("filename") String filename) {
        try {
            // 验证文件名，防止路径遍历攻击
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return new ResponseEntity<>("文件名无效", HttpStatus.BAD_REQUEST);
            }
            
            // 构建文件路径
            Path filePath = Paths.get(uploadDir, filename);
            
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                return new ResponseEntity<>("文件不存在", HttpStatus.NOT_FOUND);
            }
            
            // 检查是否是图片文件
            String contentType = Files.probeContentType(filePath);
            if (contentType == null || !contentType.startsWith("image/")) {
                return new ResponseEntity<>("只能删除图片文件", HttpStatus.BAD_REQUEST);
            }
            
            // 删除文件
            Files.delete(filePath);
            return ResponseEntity.ok("文件删除成功");
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("文件删除失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 响应类
    public static class FileUploadResponse {
        private String url;
        private String filename;

        public FileUploadResponse(String url, String filename) {
            this.url = url;
            this.filename = filename;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }
}
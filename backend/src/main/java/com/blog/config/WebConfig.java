package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.resource-path}")
    private String resourcePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源处理器，将/upload/**路径映射到上传目录
        // 使用file://前缀确保能正确访问文件系统中的文件
        registry.addResourceHandler(resourcePath + "/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600); // 设置缓存时间，提高性能
    }
}
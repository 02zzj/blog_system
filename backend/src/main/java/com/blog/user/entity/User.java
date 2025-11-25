package com.blog.user.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user", schema = "blog_db", catalog = "blog_db")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    @Column(columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 用户简介
    @Column(columnDefinition = "VARCHAR(500)")
    private String introduction;
    
    // 头像URL
    private String avatar;
    
    // 用户角色：USER(普通用户), ADMIN(管理员)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    private String role = "USER";
    
    // 账号状态：true(启用), false(禁用)
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean enabled = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
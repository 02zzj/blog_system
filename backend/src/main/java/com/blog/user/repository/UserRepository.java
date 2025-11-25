package com.blog.user.repository;

import com.blog.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    
    // 获取所有用户（用于管理员）
    Page<User> findAll(Pageable pageable);
    
    // 根据关键词搜索用户（通过邮箱或昵称）
    Page<User> findByEmailContainingOrNicknameContaining(String emailKeyword, String nicknameKeyword, Pageable pageable);
    
    // 仅根据邮箱搜索
    Page<User> findByEmailContaining(String keyword, Pageable pageable);
    
    // 仅根据昵称搜索
    Page<User> findByNicknameContaining(String keyword, Pageable pageable);
    
    // 查询当天登录的用户数量（使用updatedAt作为登录时间）
    long countByUpdatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
    
    // 查询当天活跃用户列表（使用updatedAt作为登录时间）
    Page<User> findByUpdatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay, Pageable pageable);
    
    // 根据更新时间查询当天活跃用户（简化版本）
    Page<User> findByUpdatedAtAfter(LocalDateTime startOfDay, Pageable pageable);
    
    // 按昵称搜索活跃用户
    Page<User> findByNicknameContainingAndUpdatedAtAfter(String nickname, LocalDateTime startOfDay, Pageable pageable);
    
    // 按邮箱搜索活跃用户
    Page<User> findByEmailContainingAndUpdatedAtAfter(String email, LocalDateTime startOfDay, Pageable pageable);
    
    // 按邮箱或昵称搜索活跃用户
    Page<User> findByEmailContainingOrNicknameContainingAndUpdatedAtAfter(String emailKeyword, String nicknameKeyword, LocalDateTime startOfDay, Pageable pageable);
}
package com.blog.user.util;

import com.blog.user.entity.User;

/**
 * 用户角色工具类，用于权限检查
 */
public class UserRoleUtil {
    
    /**
     * 检查用户是否为管理员
     * @param user 用户对象
     * @return 是否为管理员
     */
    public static boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    /**
     * 检查用户是否有权限操作指定文章
     * @param user 用户对象
     * @param authorId 文章作者ID
     * @return 是否有权限
     */
    public static boolean hasPermission(User user, Long authorId) {
        return user != null && (isAdmin(user) || user.getId().equals(authorId));
    }
}
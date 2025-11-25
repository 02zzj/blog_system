package com.blog.user.service;

import com.blog.user.dto.UserLoginDTO;
import com.blog.user.dto.UserRegisterDTO;
import com.blog.user.dto.ForgotPasswordDTO;
import com.blog.user.dto.VerifyCodeDTO;
import com.blog.user.dto.ResetPasswordDTO;
import com.blog.user.dto.UpdateUserDTO;
import com.blog.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    void sendVerificationCode(String email);
    User register(UserRegisterDTO dto);
    User login(UserLoginDTO dto);
    User getCurrentUser();
    void sendForgotPasswordCode(ForgotPasswordDTO forgotPasswordDTO);
    boolean verifyForgotPasswordCode(VerifyCodeDTO verifyCodeDTO);
    void resetPassword(ResetPasswordDTO resetPasswordDTO);
    User updateUser(UpdateUserDTO updateUserDTO);
    boolean isNicknameAvailable(String nickname, Long excludeUserId);
    
    // 管理员功能
    Page<User> getAllUsers(Pageable pageable);
    Page<User> searchUsers(String keyword, String searchType, Pageable pageable);
    void disableUser(Long userId);
    void enableUser(Long userId);
    void deleteUser(Long userId);
    
    // 获取当天活跃用户数
    long getActiveUserCount();
    
    // 获取当天活跃用户列表
    Page<User> getActiveUsers(Pageable pageable);
    
    // 搜索当天活跃用户
    Page<User> searchActiveUsers(String keyword, String searchType, Pageable pageable);
}
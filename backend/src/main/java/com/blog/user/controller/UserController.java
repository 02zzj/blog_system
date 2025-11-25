package com.blog.user.controller;

import com.blog.common.ResponseResult;
import com.blog.security.JwtProvider;
import com.blog.user.dto.EmailDTO;
import com.blog.user.dto.UserLoginDTO;
import com.blog.user.dto.UserRegisterDTO;
import com.blog.user.dto.ForgotPasswordDTO;
import com.blog.user.dto.VerifyCodeDTO;
import com.blog.user.dto.ResetPasswordDTO;
import com.blog.user.dto.UpdateUserDTO;
import com.blog.user.entity.User;
import com.blog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProvider jwtProvider;

    @PostMapping("/send-verification")
    public ResponseResult<Void> sendCode(@RequestBody EmailDTO dto) {
        userService.sendVerificationCode(dto.getEmail());
        return ResponseResult.success(null);
    }

    @PostMapping("/register")
    public ResponseResult<Void> register(@RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return ResponseResult.success(null);
    }

    @PostMapping("/login")
    public ResponseResult<Map<String, String>> login(@RequestBody UserLoginDTO dto) {
        User user = userService.login(dto);
        Map<String, String> result = new HashMap<>();
        result.put("token", jwtProvider.generateToken(user.getId(), user.getEmail()));
        return ResponseResult.success(result);
    }

    @GetMapping("/me")
    public ResponseResult<User> me() {
        return ResponseResult.success(userService.getCurrentUser());
    }
    
    @GetMapping("/nickname/available")
    public ResponseResult<Map<String, Boolean>> isNicknameAvailable(@RequestParam String nickname, @RequestParam(required = false) Long excludeUserId) {
        boolean available = userService.isNicknameAvailable(nickname, excludeUserId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        return ResponseResult.success(result);
    }
    
    @PutMapping("/me")
    public ResponseResult<User> update(@RequestBody UpdateUserDTO updateUserDTO) {
        return ResponseResult.success(userService.updateUser(updateUserDTO));
    }
    
    @PostMapping("/forgot-password/send-code")
    public ResponseResult<Void> sendForgotPasswordCode(@RequestBody ForgotPasswordDTO dto) {
        userService.sendForgotPasswordCode(dto);
        return ResponseResult.success(null);
    }
    
    @PostMapping("/forgot-password/verify-code")
    public ResponseResult<Map<String, Boolean>> verifyForgotPasswordCode(@RequestBody VerifyCodeDTO dto) {
        boolean isValid = userService.verifyForgotPasswordCode(dto);
        Map<String, Boolean> result = new HashMap<>();
        result.put("valid", isValid);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/forgot-password/reset")
    public ResponseResult<Void> resetPassword(@RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return ResponseResult.success(null);
    }
    
    // 管理员功能API
    
    /**
     * 获取所有用户列表（管理员）
     */
    @GetMapping("/admin")
    public ResponseResult<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String searchType) {
        // 构建排序参数
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortBy));
        
        // 根据是否有关键词决定调用哪个方法
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userService.searchUsers(keyword, searchType, pageable);
        } else {
            users = userService.getAllUsers(pageable);
        }
        
        return ResponseResult.success(users);
    }
    
    /**
     * 禁用用户（管理员）
     */
    @PutMapping("/admin/{userId}/disable")
    public ResponseResult<Void> disableUser(@PathVariable Long userId) {
        userService.disableUser(userId);
        return ResponseResult.success(null);
    }
    
    /**
     * 启用用户（管理员）
     */
    @PutMapping("/admin/{userId}/enable")
    public ResponseResult<Void> enableUser(@PathVariable Long userId) {
        userService.enableUser(userId);
        return ResponseResult.success(null);
    }
    
    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/admin/{userId}")
    public ResponseResult<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseResult.success(null);
    }
    
    /**
     * 获取当天活跃用户数（使用updatedAt字段作为登录时间）
     */
    @GetMapping("/admin/active-count")
    public ResponseResult<Long> getActiveUserCount() {
        long activeCount = userService.getActiveUserCount();
        return ResponseResult.success(activeCount);
    }
    
    /**
     * 获取当天活跃用户列表（使用updatedAt字段作为登录时间）
     */
    @GetMapping("/admin/active-users")
    public ResponseResult<Page<User>> getActiveUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String searchType) {
        // 构建排序参数
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortBy));
        
        // 根据是否有关键词决定调用哪个方法
        Page<User> activeUsers;
        if (keyword != null && !keyword.trim().isEmpty()) {
            activeUsers = userService.searchActiveUsers(keyword, searchType, pageable);
        } else {
            activeUsers = userService.getActiveUsers(pageable);
        }
        
        return ResponseResult.success(activeUsers);
    }
}
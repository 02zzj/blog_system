package com.blog.user.service;

import com.blog.user.dto.UserLoginDTO;
import com.blog.user.dto.UserRegisterDTO;
import com.blog.user.entity.User;

public interface UserService {
    void sendVerificationCode(String email);
    User register(UserRegisterDTO dto);
    User login(UserLoginDTO dto);
    User getCurrentUser();
}
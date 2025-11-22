package com.blog.user.service;

import com.blog.user.dto.UserLoginDTO;
import com.blog.user.dto.UserRegisterDTO;
import com.blog.user.dto.ForgotPasswordDTO;
import com.blog.user.dto.VerifyCodeDTO;
import com.blog.user.dto.ResetPasswordDTO;
import com.blog.user.entity.User;

public interface UserService {
    void sendVerificationCode(String email);
    User register(UserRegisterDTO dto);
    User login(UserLoginDTO dto);
    User getCurrentUser();
    void sendForgotPasswordCode(ForgotPasswordDTO forgotPasswordDTO);
    boolean verifyForgotPasswordCode(VerifyCodeDTO verifyCodeDTO);
    void resetPassword(ResetPasswordDTO resetPasswordDTO);
}
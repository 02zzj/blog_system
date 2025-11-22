package com.blog.user.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String email;
    private String code;
    private String newPassword;
    private String confirmPassword;
}
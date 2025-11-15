package com.blog.user.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String email;
    private String password;
    private String nickname;
    private String code;
}
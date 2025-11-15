package com.blog.user.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String email;
    private String password;
}
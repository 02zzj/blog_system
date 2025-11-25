package com.blog.user.dto;

import lombok.Data;

@Data
public class UpdateUserDTO {
    private String nickname;
    private String introduction;
    private String avatar;
}
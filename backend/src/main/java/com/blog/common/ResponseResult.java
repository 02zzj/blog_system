package com.blog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult<T> {
    private Integer code;
    private String message;
    private T data;

    // 成功响应
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(200, "success", data);
    }

    // 错误响应
    public static <T> ResponseResult<T> error(Integer code, String message) {
        return new ResponseResult<>(code, message, null);
    }

    // 简单错误响应
    public static <T> ResponseResult<T> error(String message) {
        return new ResponseResult<>(400, message, null);
    }
}
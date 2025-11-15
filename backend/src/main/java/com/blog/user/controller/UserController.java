package com.blog.user.controller;

import com.blog.common.ResponseResult;
import com.blog.security.JwtProvider;
import com.blog.user.dto.EmailDTO;
import com.blog.user.dto.UserLoginDTO;
import com.blog.user.dto.UserRegisterDTO;
import com.blog.user.entity.User;
import com.blog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
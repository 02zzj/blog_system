package com.blog.user.service.impl;


import com.blog.user.dto.UserLoginDTO;
import com.blog.user.dto.UserRegisterDTO;
import com.blog.user.entity.User;
import com.blog.user.repository.UserRepository;
import com.blog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Resource
    private PasswordEncoder passwordEncoder;



    // 用于存储验证码，实际项目中应使用Redis
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationCode(String email) {
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 存储验证码（实际项目中应设置过期时间）
        verificationCodes.put(email, code);
        
        // 模拟发送邮件，实际项目中应集成邮件服务
        System.out.println("向邮箱 " + email + " 发送验证码: " + code);
    }

    @Override
    public User register(UserRegisterDTO dto) {
        // 验证邮箱是否已存在
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 验证验证码
        String storedCode = verificationCodes.get(dto.getEmail());
        if (storedCode == null || !storedCode.equals(dto.getCode())) {
            throw new RuntimeException("验证码错误");
        }

        // 创建用户
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());

        return userRepository.save(user);
    }

    @Override
    public User login(UserLoginDTO dto) {
        // 验证邮箱和密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息
        return userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("未登录");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
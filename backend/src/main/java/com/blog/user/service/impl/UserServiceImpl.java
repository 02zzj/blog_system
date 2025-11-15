package com.blog.user.service.impl;


import com.blog.user.dto.UserLoginDTO;
import com.blog.user.dto.UserRegisterDTO;
import com.blog.user.entity.User;
import com.blog.user.repository.UserRepository;
import com.blog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Resource
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JavaMailSender mailSender;

    // 用于存储验证码，实际项目中应使用Redis
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    // 用于存储验证码创建时间，实际项目中应使用Redis的过期机制
    private final Map<String, Long> verificationCodeCreationTimes = new ConcurrentHashMap<>();
    // 验证码过期时间（分钟）
    private static final int VERIFICATION_CODE_EXPIRATION = 5;

    @Override
    public void sendVerificationCode(String email) {
        // 检查是否在冷却期内（避免频繁发送）
        if (isInCoolingPeriod(email)) {
            throw new RuntimeException("验证码发送过于频繁，请稍后再试");
        }
        
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 存储验证码和创建时间
        verificationCodes.put(email, code);
        verificationCodeCreationTimes.put(email, System.currentTimeMillis());
        
        // 发送邮件
        sendEmail(email, "博客系统注册验证码", "您的注册验证码是: " + code + "，有效期5分钟，请及时使用。");
        
        System.out.println("向邮箱 " + email + " 发送验证码: " + code);
    }
    
    /**
     * 发送邮件
     */
    private void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2217594081@qq.com"); // 发件人
        message.setTo(to); // 收件人
        message.setSubject(subject); // 邮件主题
        message.setText(content); // 邮件内容
        
        mailSender.send(message);
    }
    
    /**
     * 检查验证码是否过期
     */
    private boolean isVerificationCodeExpired(String email) {
        Long creationTime = verificationCodeCreationTimes.get(email);
        if (creationTime == null) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - creationTime);
        return diffMinutes >= VERIFICATION_CODE_EXPIRATION;
    }
    
    /**
     * 检查是否在冷却期内（避免频繁发送）
     */
    private boolean isInCoolingPeriod(String email) {
        Long creationTime = verificationCodeCreationTimes.get(email);
        if (creationTime == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(currentTime - creationTime);
        return diffSeconds < 60; // 1分钟冷却期
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
        
        // 检查验证码是否过期
        if (isVerificationCodeExpired(dto.getEmail())) {
            throw new RuntimeException("验证码已过期");
        }
        
        // 验证成功后移除验证码
        verificationCodes.remove(dto.getEmail());
        verificationCodeCreationTimes.remove(dto.getEmail());

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
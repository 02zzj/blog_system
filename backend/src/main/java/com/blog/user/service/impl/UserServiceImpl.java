package com.blog.user.service.impl;


import com.blog.article.entity.Article;
import com.blog.article.repository.ArticleRepository;
import com.blog.article.service.ArticleService;
import com.blog.file.util.FileUtils;
import com.blog.user.dto.ForgotPasswordDTO;
import com.blog.user.dto.ResetPasswordDTO;
import com.blog.user.dto.UpdateUserDTO;
import com.blog.user.dto.UserLoginDTO;
import com.blog.user.dto.UserRegisterDTO;
import com.blog.user.dto.VerifyCodeDTO;
import com.blog.user.entity.User;
import com.blog.user.repository.UserRepository;
import com.blog.user.service.UserService;
import com.blog.user.util.UserRoleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private FileUtils fileUtils;

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

        // 检查昵称是否已存在
        if (!isNicknameAvailable(dto.getNickname(), null)) {
            throw new RuntimeException("该昵称已被使用");
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
        // 先检查用户是否存在且账号状态
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
                
        // 检查账号是否被禁用
        if (!user.getEnabled()) {
            throw new RuntimeException("您的账号已被封禁，请联系管理员解决");
        }
        
        // 验证邮箱和密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 更新updatedAt字段作为登录时间
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        return user;
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

    @Override
    public void sendForgotPasswordCode(ForgotPasswordDTO forgotPasswordDTO) {
        String email = forgotPasswordDTO.getEmail();
        
        // 检查用户是否存在
        if (!userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查是否在冷却期内
        if (isInCoolingPeriod(email)) {
            throw new RuntimeException("验证码发送过于频繁，请稍后再试");
        }
        
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 存储验证码和创建时间
        verificationCodes.put(email, code);
        verificationCodeCreationTimes.put(email, System.currentTimeMillis());
        
        // 发送邮件
        sendEmail(email, "博客系统重置密码验证码", "您的重置密码验证码是: " + code + "，有效期5分钟，请及时使用。");
        
        System.out.println("向邮箱 " + email + " 发送重置密码验证码: " + code);
    }

    @Override
    public boolean verifyForgotPasswordCode(VerifyCodeDTO verifyCodeDTO) {
        String email = verifyCodeDTO.getEmail();
        String code = verifyCodeDTO.getCode();
        
        // 检查验证码是否存在
        String storedCode = verificationCodes.get(email);
        if (storedCode == null || !storedCode.equals(code)) {
            return false;
        }
        
        // 检查验证码是否过期
        if (isVerificationCodeExpired(email)) {
            return false;
        }
        
        return true;
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        String email = resetPasswordDTO.getEmail();
        String code = resetPasswordDTO.getCode();
        String newPassword = resetPasswordDTO.getNewPassword();
        String confirmPassword = resetPasswordDTO.getConfirmPassword();
        
        // 验证两次密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        // 验证验证码
        VerifyCodeDTO verifyCodeDTO = new VerifyCodeDTO();
        verifyCodeDTO.setEmail(email);
        verifyCodeDTO.setCode(code);
        if (!verifyForgotPasswordCode(verifyCodeDTO)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        // 获取用户并更新密码
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // 重置成功后移除验证码
        verificationCodes.remove(email);
        verificationCodeCreationTimes.remove(email);
    }
    
    @Override
    public long getActiveUserCount() {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        if (!UserRoleUtil.isAdmin(currentUser)) {
            throw new RuntimeException("只有管理员可以访问此功能");
        }
        
        // 计算当天的开始和结束时间
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        
        // 统计当天登录的用户数
        return userRepository.countByUpdatedAtBetween(startOfDay, endOfDay);
    }
    
    @Override
    public Page<User> getActiveUsers(Pageable pageable) {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        if (!UserRoleUtil.isAdmin(currentUser)) {
            throw new RuntimeException("只有管理员可以访问此功能");
        }
        
        // 计算当天的开始和结束时间
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        
        // 查询当天活跃用户列表
        return userRepository.findByUpdatedAtBetween(startOfDay, endOfDay, pageable);
    }
    
    @Override
    public Page<User> searchActiveUsers(String keyword, String searchType, Pageable pageable) {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        if (!UserRoleUtil.isAdmin(currentUser)) {
            throw new RuntimeException("只有管理员可以访问此功能");
        }
        
        // 计算当天的开始时间
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getActiveUsers(pageable);
        }
        
        // 根据搜索类型进行不同的搜索
        if ("nickname".equals(searchType)) {
            // 只按昵称搜索
            return userRepository.findByNicknameContainingAndUpdatedAtAfter(keyword, startOfDay, pageable);
        } else if ("email".equals(searchType)) {
            // 只按邮箱搜索
            return userRepository.findByEmailContainingAndUpdatedAtAfter(keyword, startOfDay, pageable);
        } else {
            // 默认或'all'类型，同时按邮箱和昵称搜索
            return userRepository.findByEmailContainingOrNicknameContainingAndUpdatedAtAfter(keyword, keyword, startOfDay, pageable);
        }
    }
    
    @Override
    public boolean isNicknameAvailable(String nickname, Long excludeUserId) {
        Optional<User> userOptional = userRepository.findByNickname(nickname);
        // 如果找不到用户，或者找到的用户ID就是要排除的ID（编辑自己资料时），则昵称可用
        return !userOptional.isPresent() || (excludeUserId != null && userOptional.get().getId().equals(excludeUserId));
    }

    @Override
    public User updateUser(UpdateUserDTO updateUserDTO) {
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        
        // 更新用户信息
        if (updateUserDTO.getNickname() != null && !updateUserDTO.getNickname().equals(currentUser.getNickname())) {
            // 如果要修改昵称，检查新昵称是否可用（排除当前用户自己）
            if (!isNicknameAvailable(updateUserDTO.getNickname(), currentUser.getId())) {
                throw new RuntimeException("该昵称已被使用");
            }
            currentUser.setNickname(updateUserDTO.getNickname());
        }
        if (updateUserDTO.getIntroduction() != null) {
            currentUser.setIntroduction(updateUserDTO.getIntroduction());
        }
        if (updateUserDTO.getAvatar() != null) {
            currentUser.setAvatar(updateUserDTO.getAvatar());
        }
        
        // 保存更新后的用户信息
        return userRepository.save(currentUser);
    }
    
    // 管理员功能实现
    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        if (!UserRoleUtil.isAdmin(currentUser)) {
            throw new RuntimeException("只有管理员可以访问此功能");
        }
        
        return userRepository.findAll(pageable);
    }
    
    @Override
    public Page<User> searchUsers(String keyword, String searchType, Pageable pageable) {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        if (!UserRoleUtil.isAdmin(currentUser)) {
            throw new RuntimeException("只有管理员可以访问此功能");
        }
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers(pageable);
        }
        
        // 根据搜索类型进行不同的搜索
        if ("nickname".equals(searchType)) {
            // 只按昵称搜索
            return userRepository.findByNicknameContaining(keyword, pageable);
        } else if ("email".equals(searchType)) {
            // 只按邮箱搜索
            return userRepository.findByEmailContaining(keyword, pageable);
        } else {
            // 默认或'all'类型，同时按邮箱和昵称搜索
            return userRepository.findByEmailContainingOrNicknameContaining(keyword, keyword, pageable);
        }
    }
    
    @Override
    public void disableUser(Long userId) {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        if (!UserRoleUtil.isAdmin(currentUser)) {
            throw new RuntimeException("只有管理员可以访问此功能");
        }
        
        // 不允许禁用自己
        if (currentUser.getId().equals(userId)) {
            throw new RuntimeException("不能禁用自己的账号");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 不允许禁用其他管理员
        if (UserRoleUtil.isAdmin(user)) {
            throw new RuntimeException("不能禁用其他管理员账号");
        }
        
        user.setEnabled(false);
        userRepository.save(user);
    }
    
    @Override
    public void enableUser(Long userId) {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        if (!UserRoleUtil.isAdmin(currentUser)) {
            throw new RuntimeException("只有管理员可以访问此功能");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        user.setEnabled(true);
        userRepository.save(user);
    }
    
    @Override
    public void deleteUser(Long userId) {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        if (!UserRoleUtil.isAdmin(currentUser)) {
            throw new RuntimeException("只有管理员可以访问此功能");
        }
        
        // 不允许删除自己
        if (currentUser.getId().equals(userId)) {
            throw new RuntimeException("不能删除自己的账号");
        }
        
        User userToDelete = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 不允许删除其他管理员
        if (UserRoleUtil.isAdmin(userToDelete)) {
            throw new RuntimeException("不能删除其他管理员账号");
        }
        
        // 实现级联删除功能
        
        // 1. 删除用户的所有评论（通过实体关系级联删除）
        // 由于Comment表设置了ON DELETE CASCADE，删除用户时会自动级联删除评论
        
        // 2. 删除用户的所有文章（使用ArticleService.deleteArticle确保相关图片被清理）
        // 使用Page来获取所有文章，设置一个足够大的page size来获取所有文章
        Pageable pageable = PageRequest.of(0, 1000); // 假设用户最多有1000篇文章
        Page<Article> userArticlesPage = articleRepository.findByAuthorId(userId, pageable);
        for (Article article : userArticlesPage.getContent()) {
            // 使用articleService.deleteArticle方法删除文章，该方法会自动清理相关图片
            try {
                articleService.deleteArticle(article.getId(), currentUser);
                logger.info("已删除用户ID={}的文章ID={}及其相关图片", userId, article.getId());
            } catch (Exception e) {
                logger.error("删除文章ID={}失败: {}", article.getId(), e.getMessage());
            }
        }
        
        // 3. 删除用户的头像文件
        if (userToDelete.getAvatar() != null && !userToDelete.getAvatar().isEmpty()) {
            try {
                // 从头像URL中提取文件名并删除文件
                String filename = fileUtils.extractFilenameFromUrl(userToDelete.getAvatar());
                if (filename != null) {
                    boolean deleted = fileUtils.deleteFile(filename);
                    if (deleted) {
                        logger.info("已删除用户头像: {}", userToDelete.getAvatar());
                    } else {
                        logger.warn("删除用户头像失败: {}", userToDelete.getAvatar());
                    }
                }
            } catch (Exception e) {
                logger.error("处理用户头像删除时出错: {}", e.getMessage());
            }
        }
        
        // 4. 删除用户账号
        userRepository.delete(userToDelete);
    }
}
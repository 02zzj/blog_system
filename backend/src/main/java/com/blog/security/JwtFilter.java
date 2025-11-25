package com.blog.security;

import com.blog.user.entity.User;
import com.blog.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null && jwtProvider.validateToken(jwt)) {
                String email = jwtProvider.getUserEmailFromJWT(jwt);
                
                // 先检查用户是否存在且账号状态
                User user = userRepository.findByEmail(email)
                        .orElse(null);
                
                // 如果用户不存在，清除认证并跳过
                if (user == null) {
                    SecurityContextHolder.clearContext();
                    return;
                }
                
                // 如果账号被禁用，设置错误响应并返回
                if (!user.getEnabled()) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"您的账号已被封禁，请联系管理员解决\"}");
                    return;
                }

                // 加载用户详情用于认证
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 更新用户登录时间（updatedAt字段）
                // 注意：这里使用findByEmail可能会有性能问题，但对于演示系统是可接受的
                // 生产环境可以考虑使用缓存或其他优化手段
                // 检查是否需要更新登录时间（避免每次请求都更新）
                // 只在用户当天第一次使用token访问时更新登录时间
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
                
                // 如果用户今天还没有更新过登录时间，则更新
                if (user.getUpdatedAt().isBefore(startOfDay)) {
                    user.setUpdatedAt(now);
                    userRepository.save(user);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
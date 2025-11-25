package com.blog.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                        .antMatchers("/api/users/send-verification", "/api/users/register", "/api/users/login", 
                                "/api/users/forgot-password/send-code", "/api/users/forgot-password/verify-code", 
                                "/api/users/forgot-password/reset","/api/users/nickname/available").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/articles", "/api/articles/*").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                        // 允许对上传文件的公共访问
                        .antMatchers("/uploads/**").permitAll()
                        .antMatchers("/api/users/me").authenticated()
                        // 仅POST、PUT、DELETE方法需要认证
                        .antMatchers(HttpMethod.POST, "/api/articles/*", "/api/comments/**").authenticated()
                        .antMatchers(HttpMethod.PUT, "/api/articles/*", "/api/comments/**").authenticated()
                        .antMatchers(HttpMethod.DELETE, "/api/articles/*", "/api/comments/**").authenticated()
                        .anyRequest().authenticated()
                        .and()
                .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用DelegatingPasswordEncoder，默认支持多种密码编码方式
        // 注意：生产环境应该明确指定使用bcrypt等强加密算法
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
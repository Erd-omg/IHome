package com.ihome.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 测试环境 Web Security 配置
 * 允许所有请求通过，用于在 WebMvcTest 中禁用 Security 检查
 * 禁用方法级安全，允许 @PreAuthorize 注解被绕过
 */
@TestConfiguration
@Profile("test")
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = false)  // 禁用方法级安全，允许所有请求通过
public class TestWebSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}


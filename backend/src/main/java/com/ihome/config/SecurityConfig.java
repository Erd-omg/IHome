package com.ihome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 - 不需要包含 context-path，Spring Security 会自动处理
                        .requestMatchers(
                                "/health",
                                "/swagger-ui.html", 
                                "/swagger-ui/**", 
                                "/v3/api-docs/**",
                                "/students/register",
                                "/students/login",
                                "/admin/login",
                                "/auth/refresh",
                                "/password/**"
                        ).permitAll()
                        // 学生接口：登录注册公开，其他需要学生角色
                        .requestMatchers("/students/register", "/students/login").permitAll()
                        .requestMatchers("/students/**").hasAnyRole("STUDENT", "ADMIN")
                        // 管理员接口需要管理员角色
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 智能分配接口需要管理员角色
                        .requestMatchers("/allocation/**").hasRole("ADMIN")
                        // 宿舍调换接口需要学生或管理员角色
                        .requestMatchers("/switches/**").hasAnyRole("STUDENT", "ADMIN")
                        // 统计报表接口需要管理员角色
                        .requestMatchers("/statistics/**").hasRole("ADMIN")
                        // 文件上传接口需要学生或管理员角色
                        .requestMatchers("/files/**").hasAnyRole("STUDENT", "ADMIN")
                        // 通知接口需要学生或管理员角色
                        .requestMatchers("/notifications/**").hasAnyRole("STUDENT", "ADMIN")
                        // 宿舍接口需要学生或管理员角色
                        .requestMatchers("/dorms/**").hasAnyRole("STUDENT", "ADMIN")
                        // 维修接口需要学生或管理员角色
                        .requestMatchers("/repairs/**").hasAnyRole("STUDENT", "ADMIN")
                        // 生活习惯标签接口需要学生或管理员角色
                        .requestMatchers("/lifestyle-tags/**").hasAnyRole("STUDENT", "ADMIN")
                        // 问卷接口需要学生或管理员角色
                        .requestMatchers("/questionnaire/**").hasAnyRole("STUDENT", "ADMIN")
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}



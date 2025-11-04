package com.ihome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome.common.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器测试
 */
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
    }
)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 测试前准备
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        // 准备测试数据
        String refreshToken = "valid-refresh-token";
        String userId = "2024001";
        String userType = "student";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        // 模拟JWT工具类
        when(jwtUtils.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getTokenTypeFromToken(refreshToken)).thenReturn("refresh");
        when(jwtUtils.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(jwtUtils.getUserTypeFromToken(refreshToken)).thenReturn(userType);
        when(jwtUtils.generateAccessToken(userId, userType)).thenReturn(newAccessToken);
        when(jwtUtils.generateRefreshToken(userId, userType)).thenReturn(newRefreshToken);

        // 准备请求体
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", refreshToken);

        // 执行测试
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(newRefreshToken));

        // 验证调用
        verify(jwtUtils).validateToken(refreshToken);
        verify(jwtUtils).getTokenTypeFromToken(refreshToken);
        verify(jwtUtils).generateAccessToken(userId, userType);
        verify(jwtUtils).generateRefreshToken(userId, userType);
    }

    @Test
    void testRefreshToken_InvalidToken() throws Exception {
        // 准备测试数据
        String invalidToken = "invalid-token";
        
        // 模拟JWT工具类
        when(jwtUtils.validateToken(invalidToken)).thenReturn(false);

        // 准备请求体
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", invalidToken);

        // 执行测试
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无效的刷新token"));

        // 验证调用
        verify(jwtUtils).validateToken(invalidToken);
        verify(jwtUtils, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    void testRefreshToken_WrongTokenType() throws Exception {
        // 准备测试数据
        String accessToken = "access-token";
        
        // 模拟JWT工具类
        when(jwtUtils.validateToken(accessToken)).thenReturn(true);
        when(jwtUtils.getTokenTypeFromToken(accessToken)).thenReturn("access");

        // 准备请求体
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", accessToken);

        // 执行测试
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无效的token类型"));

        // 验证调用
        verify(jwtUtils).validateToken(accessToken);
        verify(jwtUtils).getTokenTypeFromToken(accessToken);
        verify(jwtUtils, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    void testRefreshToken_Exception() throws Exception {
        // 准备测试数据
        String refreshToken = "valid-refresh-token";
        
        // 模拟JWT工具类抛出异常
        when(jwtUtils.validateToken(refreshToken)).thenThrow(new RuntimeException("Token验证失败"));

        // 准备请求体
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", refreshToken);

        // 执行测试
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("token刷新失败")));
    }
}


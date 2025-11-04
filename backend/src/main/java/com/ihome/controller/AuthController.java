package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.common.JwtResponse;
import com.ihome.common.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/refresh")
    public ApiResponse<JwtResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            // 验证refresh token
            if (!jwtUtils.validateToken(refreshToken)) {
                return ApiResponse.error("无效的刷新token");
            }
            
            // 检查是否为refresh token
            String tokenType = jwtUtils.getTokenTypeFromToken(refreshToken);
            if (!"refresh".equals(tokenType)) {
                return ApiResponse.error("无效的token类型");
            }
            
            // 获取用户信息
            String userId = jwtUtils.getUserIdFromToken(refreshToken);
            String userType = jwtUtils.getUserTypeFromToken(refreshToken);
            
            // 生成新的access token
            String newAccessToken = jwtUtils.generateAccessToken(userId, userType);
            String newRefreshToken = jwtUtils.generateRefreshToken(userId, userType);
            
            JwtResponse jwtResponse = new JwtResponse(newAccessToken, newRefreshToken, 600L, null);
            return ApiResponse.ok(jwtResponse);
            
        } catch (Exception e) {
            return ApiResponse.error("token刷新失败: " + e.getMessage());
        }
    }

    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}


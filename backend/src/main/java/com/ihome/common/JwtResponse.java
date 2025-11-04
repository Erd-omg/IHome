package com.ihome.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT认证响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    @JsonProperty("accessToken")
    private String accessToken;
    
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    @JsonProperty("tokenType")
    private String tokenType = "Bearer";
    
    @JsonProperty("expiresIn")
    private Long expiresIn;
    
    @JsonProperty("userInfo")
    private Object userInfo;

    public JwtResponse(String accessToken, String refreshToken, Long expiresIn, Object userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
    }

    // 手动添加getter方法以确保序列化正常工作
    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public Object getUserInfo() {
        return userInfo;
    }
}


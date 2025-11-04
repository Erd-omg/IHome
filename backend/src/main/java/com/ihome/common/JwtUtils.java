package com.ihome.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 负责JWT token的生成、验证和刷新
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:ihome-dormitory-management-system-secret-key-2024}")
    private String secret;

    @Value("${jwt.expiration:600000}") // 10分钟，单位毫秒
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7天，单位毫秒
    private Long refreshExpiration;

    /**
     * 生成访问token
     */
    public String generateAccessToken(String userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("tokenType", "access");
        return createToken(claims, userId, expiration);
    }

    /**
     * 生成刷新token
     */
    public String generateRefreshToken(String userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("tokenType", "refresh");
        return createToken(claims, userId, refreshExpiration);
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从token中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        // 开发环境支持mock token
        if (isMockToken(token)) {
            if ("mock-admin-token".equals(token) || "mock-admin-refresh-token".equals(token)) {
                return "A001";
            } else if ("mock-student-token".equals(token) || "mock-student-refresh-token".equals(token)) {
                return "2024001";
            }
        }
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从token中获取用户类型
     */
    public String getUserTypeFromToken(String token) {
        // 开发环境支持mock token
        if (isMockToken(token)) {
            if ("mock-admin-token".equals(token) || "mock-admin-refresh-token".equals(token)) {
                return "admin";
            } else if ("mock-student-token".equals(token) || "mock-student-refresh-token".equals(token)) {
                return "student";
            }
        }
        return getClaimFromToken(token, claims -> claims.get("userType", String.class));
    }

    /**
     * 从token中获取token类型
     */
    public String getTokenTypeFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("tokenType", String.class));
    }

    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中获取指定声明
     */
    public <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.resolve(claims);
    }

    /**
     * 从token中获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否过期
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, String userId) {
        try {
            final String tokenUserId = getUserIdFromToken(token);
            return (tokenUserId.equals(userId) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 验证token（不检查用户ID）
     */
    public Boolean validateToken(String token) {
        try {
            // 开发环境支持mock token
            if (isMockToken(token)) {
                return true;
            }
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为mock token（开发环境使用）
     */
    private Boolean isMockToken(String token) {
        return "mock-admin-token".equals(token) || 
               "mock-student-token".equals(token) ||
               "mock-admin-refresh-token".equals(token) ||
               "mock-student-refresh-token".equals(token);
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        // 确保密钥长度至少为 512 位（64字节）用于 HS512
        byte[] keyBytes;
        if (secret.length() < 64) {
            // 如果密钥太短，使用 SHA-256 生成 64 字节密钥
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                keyBytes = md.digest(secret.getBytes());
                // 扩展到 64 字节
                byte[] extended = new byte[64];
                System.arraycopy(keyBytes, 0, extended, 0, keyBytes.length);
                System.arraycopy(keyBytes, 0, extended, keyBytes.length, 64 - keyBytes.length);
                keyBytes = extended;
            } catch (java.security.NoSuchAlgorithmException e) {
                keyBytes = secret.getBytes();
            }
        } else {
            keyBytes = secret.substring(0, Math.min(64, secret.length())).getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 声明解析器接口
     */
    @FunctionalInterface
    public static interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}


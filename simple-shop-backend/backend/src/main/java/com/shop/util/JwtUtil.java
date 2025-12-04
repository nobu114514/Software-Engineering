package com.shop.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration = 86400000; // 24小时，单位毫秒

    @Value("${jwt.issuer}")
    private String issuer;

    /**
     * 获取密钥
     * @return SecretKey对象
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成JWT token
     * @param subject 通常是用户名
     * @param userType 用户类型（seller或customer）
     * @return JWT token
     */
    public String generateToken(String subject, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setIssuer(issuer)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析JWT token
     * @param token JWT token
     * @return Claims对象
     * @throws JwtException 如果token无效
     */
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证JWT token
     * @param token JWT token
     * @return 如果token有效返回true，否则返回false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 从token中获取用户名
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从token中获取用户类型
     * @param token JWT token
     * @return 用户类型
     */
    public String getUserTypeFromToken(String token) {
        return (String) parseToken(token).get("userType");
    }

    /**
     * 检查token是否已过期
     * @param token JWT token
     * @return 如果token已过期返回true，否则返回false
     */
    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }
}

package com.clariflow.workflow.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JWT 工具 — 生成、验证、失效 Token。
 *
 * <p>Token 有效期 {@code clariflow.jwt.expiration} 秒，默认 86400（24h）。</p>
 * <p>登出时将 Token 加入 Redis 黑名单（key=blacklist:token:{token}），TTL 对齐剩余有效期。</p>
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    private final SecretKey key;
    private final long expiration;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public JwtUtil(@Value("${clariflow.jwt.secret:ClariFlow-JWT-Secret-Key-2026-At-Least-256-Bits!!}") String secret,
                   @Value("${clariflow.jwt.expiration:86400}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /** 生成 Token */
    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 从 Token 中解析用户名 */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /** 验证 Token 是否有效（含黑名单检查） */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            if (isTokenBlacklisted(token)) {
                log.debug("Token 在黑名单中，已失效");
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 将 Token 加入黑名单，有效期为其剩余存活时间 */
    public void blacklistToken(String token) {
        if (redisTemplate == null) {
            log.warn("Redis 不可用，Token 黑名单未生效");
            return;
        }
        try {
            Claims claims = parseClaims(token);
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remaining > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(remaining));
                log.debug("Token 已加入黑名单，剩余 {} 秒", remaining / 1000);
            }
        } catch (Exception e) {
            log.debug("Token 已过期，无需加入黑名单");
        }
    }

    private boolean isTokenBlacklisted(String token) {
        if (redisTemplate == null) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
        } catch (Exception e) {
            log.debug("Redis 查询黑名单失败", e);
            return false; // 降级：Redis 不可用时放行
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

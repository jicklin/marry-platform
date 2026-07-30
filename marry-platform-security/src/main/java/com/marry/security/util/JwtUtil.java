package com.marry.security.util;

import com.marry.security.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT utility for issuing, parsing, and validating access/refresh tokens.
 *
 * <p>Token shape:
 * <ul>
 *   <li>sub = username</li>
 *   <li>uid = user id</li>
 *   <li>perms = [permission keys]</li>
 *   <li>jti = random uuid (used for refresh-token rotation and blacklist)</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties props;

    @PostConstruct
    void validateSecret() {
        String s = props.getSecret();
        if (s == null || s.length() < 32 || s.contains("replace-me")) {
            throw new IllegalStateException(
                    "marry.jwt.secret is missing, too short, or still set to the placeholder. " +
                    "Provide at least 32 random characters via JWT_SECRET env var."
            );
        }
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(Long userId, String username, Collection<String> perms) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())
                .claim("uid", userId)
                .claim("type", "access")
                .claim("perms", perms)
                .issuedAt(new Date(now))
                .expiration(new Date(now + props.getAccessTtl()))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    public String issueRefreshToken(Long userId, String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())
                .claim("uid", userId)
                .claim("type", "refresh")
                .issuedAt(new Date(now))
                .expiration(new Date(now + props.getRefreshTtl()))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    public boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public Long getRemainingMillis(Claims claims) {
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    @SuppressWarnings("unchecked")
    public List<String> getPerms(Claims claims) {
        Object raw = claims.get("perms");
        if (raw instanceof Collection<?> c) {
            return c.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    /** Catch-all wrap to surface expired tokens clearly. */
    public Claims parseOrThrow(String token) {
        try {
            return parse(token);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("invalid token: " + e.getMessage(), e);
        }
    }
}

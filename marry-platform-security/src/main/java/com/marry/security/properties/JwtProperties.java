package com.marry.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration bound from `marry.jwt.*` keys.
 */
@Data
@ConfigurationProperties(prefix = "marry.jwt")
public class JwtProperties {

    /** Secret used to sign/verify tokens. Must be at least 32 chars. */
    private String secret = "please-replace-me-with-a-32-or-more-character-secret-key";

    /** Access-token TTL in milliseconds (default 30 minutes). */
    private long accessTtl = 30 * 60 * 1000L;

    /** Refresh-token TTL in milliseconds (default 7 days). */
    private long refreshTtl = 7 * 24 * 60 * 60 * 1000L;

    /** Header name carrying the access token. */
    private String header = "Authorization";

    /** Token prefix. */
    private String tokenPrefix = "Bearer ";

    /** Redis key prefix for storing refresh tokens & blacklists. */
    private String redisKeyPrefix = "marry:token:";

    /** Redis key prefix for online-user set. */
    private String onlineKey = "marry:online";

    /** Whether to enable JWT auth (set to false for pure dev/testing). */
    private boolean enabled = true;
}
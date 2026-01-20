package com.merchant.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Optional;

@Configuration
public class RateLimitConfig {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    /**
     * Create Redis-based ProxyManager for Bucket4j rate limiting
     * Only created when rate limiting is enabled
     */
    @Bean
    @ConditionalOnProperty(name = "rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public ProxyManager<String> redisProxyManager() {
        log.info("Initializing Redis rate limiting: {}:{}", redisHost, redisPort);
        
        try {
            // Create Redis client connection
            RedisClient redisClient = RedisClient.create(
                String.format("redis://%s:%d", redisHost, redisPort)
            );
            
            // Connect with String key and byte array value codec
            StatefulRedisConnection<String, byte[]> connection = 
                redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
            
            // Build the ProxyManager with proper configuration
            ProxyManager<String> proxyManager = LettuceBasedProxyManager
                    .builderFor(connection)
                    .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)))
                    .build();
            
            log.info("✅ Rate limiting enabled successfully with Redis backend");
            return proxyManager;
            
        } catch (Exception e) {
            log.error("❌ Failed to initialize rate limiting: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize rate limiting ProxyManager", e);
        }
    }
}

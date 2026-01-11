package com.merchant.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;
    
    @Bean
    public ProxyManager<String> redisProxyManager() {
        if (!rateLimitEnabled) {
            log.warn("Rate limiting is DISABLED");
            return null;
        }
        
        log.info("Initializing Redis rate limiting: {}:{}", redisHost, redisPort);
        
        try {
            RedisClient redisClient = RedisClient.create(
                String.format("redis://%s:%d", redisHost, redisPort)
            );
            
            StatefulRedisConnection<String, byte[]> connection = 
                redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
            
            ProxyManager<String> proxyManager = LettuceBasedProxyManager
                    .builderFor(connection)
                    .build();
            
            log.info("✅ Rate limiting enabled successfully");
            return proxyManager;
            
        } catch (Exception e) {
            log.error("❌ Failed to initialize rate limiting: {}", e.getMessage());
            return null;
        }
    }
}

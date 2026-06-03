package com.clariflow.workflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 不可用时的缓存降级处理。
 *
 * <p>当 Redis 宕机或不可达时，不阻塞业务请求，仅记录日志并跳过缓存操作。
 * 业务方法会直接走数据库查询，功能不受影响，只是暂时失去缓存加速。</p>
 */
@Configuration
public class CacheErrorHandlerConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheErrorHandlerConfig.class);

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("Redis GET 失败, key={}, cache={}, 跳过缓存: {}", key, cache.getName(), e.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("Redis PUT 失败, key={}, cache={}, 跳过缓存: {}", key, cache.getName(), e.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("Redis EVICT 失败, key={}, cache={}, 跳过缓存: {}", key, cache.getName(), e.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("Redis CLEAR 失败, cache={}, 跳过缓存: {}", cache.getName(), e.getMessage());
            }
        };
    }
}

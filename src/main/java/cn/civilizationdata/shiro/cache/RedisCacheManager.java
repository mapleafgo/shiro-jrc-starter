package cn.civilizationdata.shiro.cache;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisCacheManager extends AbstractCacheManager {
    private RedisTemplate<String, Object> redisTemplate;
    private int timeOut;

    public RedisCacheManager(RedisTemplate<String, Object> redisTemplate, int tokenTimeout) {
        this.redisTemplate = redisTemplate;
        this.timeOut = tokenTimeout;
    }

    @Override
    protected Cache<String, Object> createCache(String s) throws CacheException {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return new RedisCache<>(s, redisTemplate, timeOut);
    }
}

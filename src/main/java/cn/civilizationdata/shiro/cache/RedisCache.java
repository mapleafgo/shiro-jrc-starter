package cn.civilizationdata.shiro.cache;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.session.Session;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class RedisCache<K, V> implements Cache<K, V> {
    private RedisTemplate<String, V> redisTemplate;

    private String prefix;
    private int timeOut;

    public RedisCache(String prefix, RedisTemplate<String, V> redisTemplate, int timeOut) {
        this.prefix = prefix;
        this.redisTemplate = redisTemplate;
        this.timeOut = timeOut;
    }

    @Override
    public V get(K k) throws CacheException {
        return Optional.ofNullable(k).map(x -> redisTemplate.opsForValue().get(this.getKey(x))).orElse(null);
    }

    @Override
    public V put(K k, V v) throws CacheException {
        return Optional.ofNullable(k).map(n -> {
            String key = this.getKey(n);
            redisTemplate.opsForValue().set(key, v);
            if (v instanceof Session)
                redisTemplate.expire(key, (long) (((Session) v).getTimeout() + 1.8e6), TimeUnit.MILLISECONDS);
            else
                redisTemplate.expire(key, timeOut, TimeUnit.MILLISECONDS);
            return v;
        }).orElse(null);
    }

    @Override
    public V remove(K k) throws CacheException {
        return Optional.ofNullable(k).map(x -> {
            String key = this.getKey(x);

            V v = redisTemplate.opsForValue().get(key);
            redisTemplate.delete(key);
            return v;
        }).orElse(null);
    }

    @Override
    public void clear() throws CacheException {
        redisTemplate.delete(this.getKeys());
    }

    @Override
    public int size() {
        return this.getKeys().size();
    }

    @Override
    public Set<K> keys() {
        List<String> keys = new ArrayList<>(this.getKeys());
        return keys.parallelStream().map(k -> (K) k.substring(this.prefix.length() + 1)).collect(Collectors.toSet());
    }

    @Override
    public List<V> values() {
        return redisTemplate.opsForValue().multiGet(this.getKeys());
    }

    private Set<String> getKeys() {
        return redisTemplate.keys(this.prefix + ":*");
    }

    private String getKey(K k) {
        return StrUtil.cleanBlank(this.prefix + k.toString());
    }
}

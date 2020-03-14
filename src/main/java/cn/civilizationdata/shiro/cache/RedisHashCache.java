package cn.civilizationdata.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;

public class RedisHashCache<K, V> implements Cache<K, V> {
    private RedisTemplate<String, ?> redisTemplate;
    private HashOperations<String, K, V> hashOperations;
    private String name;

    public RedisHashCache(String name, RedisTemplate<String, ?> redisTemplate) {
        this.name = name;
        this.redisTemplate = redisTemplate;
        this.hashOperations = this.redisTemplate.opsForHash();
    }

    @Override
    public V get(K k) throws CacheException {
        return hashOperations.get(this.name, k);
    }

    @Override
    public V put(K k, V v) throws CacheException {
        hashOperations.put(this.name, k, v);
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        V v = hashOperations.get(this.name, k);
        hashOperations.delete(this.name, k);
        return v;
    }

    @Override
    public void clear() throws CacheException {
        this.redisTemplate.delete(this.name);
    }

    @Override
    public int size() {
        return Math.toIntExact(hashOperations.size(this.name));
    }

    @Override
    public Set<K> keys() {
        return hashOperations.keys(this.name);
    }

    @Override
    public Collection<V> values() {
        return hashOperations.values(this.name);
    }
}

package cn.civilizationdata.shiro.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;

import java.io.Serializable;
import java.util.UUID;

@Slf4j
public class RedisSessionDAO extends EnterpriseCacheSessionDAO {
    public RedisSessionDAO(RedisCacheManager cacheManager) {
        this.setCacheManager(cacheManager);
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        return this.getActiveSessionsCache().get(sessionId);
    }

    @Override
    public String getActiveSessionsCacheName() {
        return super.getActiveSessionsCacheName() + ":";
    }

    @Override
    protected Serializable generateSessionId(Session session) {
        return UUID.randomUUID().toString();
    }
}

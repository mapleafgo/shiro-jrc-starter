package cn.civilizationdata.shiro.realm;

import cn.civilizationdata.shiro.jwt.JwtToken;
import cn.civilizationdata.shiro.utils.JWTUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class JwtCasbinRealm extends AuthorizingRealm {
    public JwtCasbinRealm() {
        this(null, null);
    }

    public JwtCasbinRealm(CredentialsMatcher matcher) {
        this(null, matcher);
    }

    public JwtCasbinRealm(CacheManager cacheManager, CredentialsMatcher matcher) {
        super(cacheManager, matcher);
        setAuthorizationCacheName("shiro:cache:");
        setCachingEnabled(false);
    }

    @Override
    public Class<?> getAuthenticationTokenClass() {
        return JwtToken.class;
    }

    @Override
    public String getName() {
        return "jwt_casbin_realm";
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return new SimpleAuthorizationInfo();
    }

    /**
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) auth;
        String token = jwtToken.getToken();
        String userId = JWTUtils.getKeyId(token);
        return new SimpleAuthenticationInfo(userId, token, this.getName());
    }

    /**
     * 清理缓存权限
     */
    public void clearCache() {
        this.doClearCache(SecurityUtils.getSubject().getPrincipals());
    }
}

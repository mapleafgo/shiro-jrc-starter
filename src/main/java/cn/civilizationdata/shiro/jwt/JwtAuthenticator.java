package cn.civilizationdata.shiro.jwt;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.Realm;

import java.util.Collection;
import java.util.Optional;

public class JwtAuthenticator extends ModularRealmAuthenticator {
    /**
     * realm选择控制
     */
    @Override
    protected AuthenticationInfo doAuthenticate(AuthenticationToken authenticationToken) throws AuthenticationException {
        assertRealmsConfigured();
        Collection<Realm> realms = getRealms();
        Optional<Realm> optionalRealm;
        optionalRealm = realms.stream().filter(realm -> {
            AuthenticatingRealm authenticatingRealm = (AuthenticatingRealm) realm;
            return authenticatingRealm.getAuthenticationTokenClass() == authenticationToken.getClass();
        }).findFirst();
        if (!optionalRealm.isPresent()) throw new IllegalStateException("无可用的Realm");
        return optionalRealm.map(realm -> doSingleRealmAuthentication(realm, authenticationToken)).orElse(null);
    }
}

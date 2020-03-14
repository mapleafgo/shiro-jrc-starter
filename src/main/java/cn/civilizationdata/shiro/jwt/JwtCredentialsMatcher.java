package cn.civilizationdata.shiro.jwt;

import cn.civilizationdata.shiro.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

@Slf4j
public class JwtCredentialsMatcher extends HashedCredentialsMatcher {
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        JwtToken jwtToken = (JwtToken) token;
        return JWTUtils.verify(jwtToken.getToken(), jwtToken.getSecret());
    }
}

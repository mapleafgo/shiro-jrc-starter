package cn.civilizationdata.shiro.jwt;

import cn.civilizationdata.shiro.utils.JWTUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.authc.AuthenticationToken;

import java.util.Map;

@Data
public class JwtToken implements AuthenticationToken {
    /**
     * 校验码
     */
    private String issuer = JwtConts.ISSUER;
    /**
     * 发布者
     */
    private String secret = JwtConts.SECRET;
    /**
     * 过期分钟数
     */
    private Integer expires = JwtConts.EXPIRES; // 分钟

    protected String userId = null;
    protected String personId = null;
    protected String username = null;

    private String token;

    public JwtToken() {
    }

    public JwtToken(String token) {
        this.token = token;
        this.userId = JWTUtils.getKeyId(token);
        this.personId = JWTUtils.getId(token);
        this.username = JWTUtils.getSubject(token);
    }

    public String generateToken() {
        this.token = JWTUtils.sign(userId, personId, username, issuer, secret, expires, null);
        return this.token;
    }

    public String generateToken(Map<String, String> claims) {
        this.token = JWTUtils.sign(userId, personId, username, issuer, secret, expires, claims);
        return this.token;
    }

    @Override
    public Object getPrincipal() {
        return this.userId;
    }

    @Override
    public Object getCredentials() {
        return this.token;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }
}

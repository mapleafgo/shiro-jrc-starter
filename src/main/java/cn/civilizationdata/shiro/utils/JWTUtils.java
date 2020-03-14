package cn.civilizationdata.shiro.utils;

import cn.civilizationdata.shiro.exception.JwtVerifyException;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * JWT工具类
 *
 * @author 慕枫
 */
@Slf4j
public class JWTUtils {
    /**
     * 生成签名,5min后过期
     *
     * @param keyId   账户id
     * @param id      人员id
     * @param subject 用户名
     * @param issuer  发布者
     * @param secret  校验码
     * @param expires 过期分钟数
     * @param claims  扩展参数
     * @return 加密的token
     */
    public static String sign(String keyId, String id, String subject,
                              String issuer, String secret, Integer expires,
                              Map<String, String> claims) {
        Date now = new Date();
        Algorithm algorithm = Algorithm.HMAC512(secret);
        JWTCreator.Builder builder = JWT.create()
            .withKeyId(keyId)
            .withJWTId(id)
            .withSubject(subject)
            .withIssuer(issuer)
            .withIssuedAt(now)
            .withExpiresAt(DateUtil.offsetMinute(now, Optional.ofNullable(expires).orElse(10)));
        if (CollectionUtil.isNotEmpty(claims))
            claims.forEach(builder::withClaim);
        return builder.sign(algorithm);
    }

    /**
     * 校验token是否正确
     *
     * @param token  token
     * @param secret 校验码
     * @return 是否正确
     */
    public static boolean verify(String token, String secret) throws JwtVerifyException {
        try {
            Algorithm algorithm = Algorithm.HMAC512(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (TokenExpiredException e) {
            throw new JwtVerifyException("token 已过期");
        } catch (JWTVerificationException e) {
            throw new JwtVerifyException("token 无效");
        }
    }

    /**
     * 获取jwt对象
     *
     * @param token jwt token
     * @return jwt对象
     */
    public static DecodedJWT getJwt(String token) {
        try {
            return JWT.decode(token);
        } catch (JWTDecodeException e) {
            log.error("token解析失败", e);
            return null;
        }
    }

    /**
     * 获得token中的personId
     *
     * @return 当前访问人员id
     */
    public static String getId(String token) {
        return Optional.ofNullable(getJwt(token)).map(DecodedJWT::getId).orElse(null);
    }

    /**
     * 获得token中的userId
     *
     * @return 当前访问账户id
     */
    public static String getKeyId(String token) {
        return Optional.ofNullable(getJwt(token)).map(DecodedJWT::getKeyId).orElse(null);
    }

    /**
     * 获得token中的当前用户名
     *
     * @return 当前访问用户名
     */
    public static String getSubject(String token) {
        return Optional.ofNullable(getJwt(token)).map(DecodedJWT::getSubject).orElse(null);
    }

    /**
     * 获得token中的字段
     *
     * @return token中包含的字符串
     */
    public static String getClaim(String token, String claimKey) {
        return Optional.ofNullable(getJwt(token)).map(jwt -> jwt.getClaim(claimKey).asString()).orElse(null);
    }
}

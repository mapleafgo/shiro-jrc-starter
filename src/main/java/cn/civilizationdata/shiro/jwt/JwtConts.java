package cn.civilizationdata.shiro.jwt;

public class JwtConts {
    /**
     * 校验码
     */
    public static String SECRET;

    /**
     * 发布者
     */
    public static String ISSUER;

    /**
     * 过期分钟数
     */
    public static Integer EXPIRES;

    public static void setSecret(String SECRET) {
        JwtConts.SECRET = SECRET;
    }

    public static void setIssuer(String ISSUER) {
        JwtConts.ISSUER = ISSUER;
    }

    public static void setExpires(Integer EXPIRES) {
        JwtConts.EXPIRES = EXPIRES;
    }
}

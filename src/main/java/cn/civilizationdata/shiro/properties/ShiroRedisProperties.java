package cn.civilizationdata.shiro.properties;

import cn.civilizationdata.shiro.casbin.properties.CasbinProperties;
import cn.civilizationdata.shiro.jwt.JwtProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties("shiro-redis-jwt")
public class ShiroRedisProperties {
    private String cachePrefix = "shiro:cache";

    private String sessionPrefix = "shiro:session";

    private int timeOut = 30 * 60 * 1000;

    private Set<String> filter;

    private CasbinProperties casbin = new CasbinProperties();

    private JwtProperties jwt = new JwtProperties();
}

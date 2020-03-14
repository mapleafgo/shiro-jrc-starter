package cn.civilizationdata.shiro.jwt;

import lombok.Data;

@Data
public class JwtProperties {
    private String secret;

    private String issuer;

    private Integer expires = 30;
}

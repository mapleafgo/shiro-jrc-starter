package cn.civilizationdata.shiro;

import cn.civilizationdata.shiro.cache.RedisCacheManager;
import cn.civilizationdata.shiro.cache.RedisSessionDAO;
import cn.civilizationdata.shiro.casbin.CasbinAdvisor;
import cn.civilizationdata.shiro.casbin.properties.CasbinProperties;
import cn.civilizationdata.shiro.casbin.subject.CasbinDefaultWebSubjectFactory;
import cn.civilizationdata.shiro.jwt.*;
import cn.civilizationdata.shiro.properties.ShiroRedisProperties;
import cn.civilizationdata.shiro.realm.JwtCasbinRealm;
import cn.hutool.core.util.StrUtil;
import cn.jcasbin.adapter.HutoolDBAdapter;
import cn.jcasbin.watcher.EtcdWatcher;
import io.etcd.jetcd.Client;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.config.web.autoconfigure.ShiroWebAutoConfiguration;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ResourceUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.sql.DataSource;
import java.util.*;

/**
 * Shiro Redis Jwt Casbin
 *
 * @author 慕枫
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({ShiroRedisProperties.class})
@AutoConfigureBefore({ShiroWebAutoConfiguration.class})
public class RedisShiroAutoConfiguration {
    @Autowired
    private ShiroRedisProperties properties;

    @PostConstruct
    public void init() {
        JwtProperties jwtProperties = properties.getJwt();
        JwtConts.setSecret(jwtProperties.getSecret());
        JwtConts.setIssuer(jwtProperties.getIssuer());
        JwtConts.setExpires(jwtProperties.getExpires());
    }

    /**
     * 开启跨域
     */
    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", corsConfiguration);
        return new FilterRegistrationBean<>(new CorsFilter(source));
    }

    @Bean
    public Client etcdClient() {
        return Client.builder().endpoints(properties.getCasbin().getEtcd()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisCacheManager redisCacheManager(RedisTemplate redisTemplate) {
        return new RedisCacheManager(redisTemplate, properties.getTimeOut());
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisSessionDAO sessionDAO(RedisCacheManager cacheManager) {
        RedisSessionDAO sessionDAO = new RedisSessionDAO(cacheManager);
        sessionDAO.setActiveSessionsCacheName(properties.getSessionPrefix());
        return sessionDAO;
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionManager sessionManager(RedisSessionDAO sessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionDAO(sessionDAO);
        return sessionManager;
    }

    @Bean
    public HashedCredentialsMatcher jwtCredentialsMatcher() {
        return new JwtCredentialsMatcher();
    }

    @Bean
    public Realm jwtRealm() {
        return new JwtCasbinRealm(jwtCredentialsMatcher());
    }

    @Bean
    @ConditionalOnMissingBean
    public Authenticator authenticator() {
        JwtAuthenticator authenticator = new JwtAuthenticator();
        authenticator.setAuthenticationStrategy(new FirstSuccessfulStrategy());
        return authenticator;
    }

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean
    public SessionsSecurityManager securityManager(List<Realm> realms, RedisCacheManager cacheManager,
                                                   Authenticator authenticator, DataSource dataSource, Client client) {
        CasbinProperties casbinProperties = properties.getCasbin();
        Model model = new Model();
        model.loadModel(ResourceUtils.getFile(casbinProperties.getModel()).getPath());
        Enforcer enforcer = new Enforcer(model, new HutoolDBAdapter(dataSource, casbinProperties.getDbtable()));
        EtcdWatcher watcher = new EtcdWatcher(client, casbinProperties.getEtcdKey());
        enforcer.setWatcher(watcher);
        watcher.startWatch();

        DefaultSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setAuthenticator(authenticator);
        securityManager.setCacheManager(cacheManager);
        securityManager.setSubjectFactory(new CasbinDefaultWebSubjectFactory(enforcer));
        securityManager.setRealms(realms);
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SessionsSecurityManager securityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager);

        Set<String> filter = properties.getFilter();

        Map<String, String> map = new LinkedHashMap<>();
        if (filter != null) filter.forEach(v -> {
            String s = StrUtil.cleanBlank(v);
            String[] strings = s.split("=");
            map.put(strings[0], strings[1]);
        });
        map.put("/**", "jwt");

        bean.setFilterChainDefinitionMap(map);

        bean.setFilters(new HashMap<String, Filter>() {
            {
                put("jwt", new JwtFilter());
            }
        });

        log.info("Shiro 拦截器工厂类注入成功");
        return bean;
    }

    /**
     * Casbin 注解拦截器
     *
     * @return 权限拦截器注入
     */
    @Bean
    @ConditionalOnMissingBean
    public CasbinAdvisor authorizationAttributeSourceAdvisor() {
        return new CasbinAdvisor();
    }
}

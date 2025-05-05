package org.socius.sociuswebbackend.config;

import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

@Configuration
@EnableRedisHttpSession()
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {

    @Autowired
    private ConfigService configService;

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

        cookieSerializer.setCookieName(configService.getString("cookie.name", "SOCIUS_SESSION"));
        cookieSerializer.setCookiePath(configService.getString("cookie.path", "/"));
        cookieSerializer.setSameSite(configService.getString("cookie.same.site", "Lax"));
        resolver.setCookieSerializer(cookieSerializer);

        return resolver;
    }

    @Bean
    public int springSessionDefaultTimeout() {
        return configService.getInt("session_timeout", 30) * 60; // Thời gian timeout mặc định là 30 phút
    }
}

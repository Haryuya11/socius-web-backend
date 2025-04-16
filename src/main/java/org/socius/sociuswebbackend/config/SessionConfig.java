package org.socius.sociuswebbackend.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400) // 24 hours, same as in properties
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("SOCIUS_SESSION");
        cookieSerializer.setCookiePath("/");
        cookieSerializer.setCookieMaxAge((int) Duration.ofDays(1).getSeconds()); // 24 hours
        cookieSerializer.setUseSecureCookie(true); // Set to true if using HTTPS
        cookieSerializer.setUseBase64Encoding(false);
        cookieSerializer.setUseHttpOnlyCookie(true);
        cookieSerializer.setSameSite("Lax");
        resolver.setCookieSerializer(cookieSerializer);
        return resolver;
    }
}

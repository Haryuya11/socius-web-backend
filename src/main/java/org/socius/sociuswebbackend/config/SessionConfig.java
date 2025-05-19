package org.socius.sociuswebbackend.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.WebSocketService;
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
@RequiredArgsConstructor
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {


    private static final Logger logger = LoggerFactory.getLogger(SessionConfig.class);
    final private ConfigService configService;
    final private WebSocketService webSocketService;

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

        cookieSerializer.setCookieName(configService.getString("cookie.name", "SOCIUS_SESSION"));
        cookieSerializer.setCookiePath(configService.getString("cookie.path", "/"));
        cookieSerializer.setCookieMaxAge(configService.getInt("session.cookie.max.age", 86400));
        cookieSerializer.setSameSite(configService.getString("cookie.same.site", "Lax"));
        cookieSerializer.setUseSecureCookie(configService.getBoolean("session.cookie.secure", true));
        cookieSerializer.setUseHttpOnlyCookie(true);

        resolver.setCookieSerializer(cookieSerializer);

        return resolver;
    }

    @Bean
    public HttpSessionListener sessionTimeoutListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                String sessionId = se.getSession().getId();
                logger.info("Phiên làm việc đã hết hạn: {}", sessionId);

                // Thông báo cho client thông qua WebSocket
                webSocketService.sendSessionInvalidationNotification(sessionId, "SESSION_TIMEOUT",
                        "Phiên làm việc đã hết hạn do không hoạt động, vui lòng đăng nhập lại");
            }
        };
    }

    @Bean
    public int springSessionDefaultTimeout() {
        return configService.getInt("session_timeout", 30) * 60; // Thời gian timeout mặc định là 30 phút
    }
}

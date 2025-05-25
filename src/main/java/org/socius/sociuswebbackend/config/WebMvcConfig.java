package org.socius.sociuswebbackend.config;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.interceptors.SessionExtensionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    final private SessionExtensionInterceptor sessionExtensionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionExtensionInterceptor).addPathPatterns("/api/**");
    }
}

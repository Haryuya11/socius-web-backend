package org.socius.sociuswebbackend.config;

import org.socius.sociuswebbackend.interceptors.SessionExtensionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private SessionExtensionInterceptor sessionExtensionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionExtensionInterceptor).addPathPatterns("/api/**");
    }
}

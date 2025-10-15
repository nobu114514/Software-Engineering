package com.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有/api/**路径的请求跨域访问
        registry.addMapping("/api/**")
                // 在开发环境中使用allowedOriginPatterns替代allowedOrigins
                // 这样可以同时使用通配符和允许凭证，解决CORS冲突
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 是否允许携带cookie
                .allowCredentials(true)
                // 预检请求的有效期（秒）
                .maxAge(3600);
    }
}

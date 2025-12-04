package com.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有/api/**路径的请求跨域访问
        registry.addMapping("/api/**")
                // 当allowCredentials为true时，不能使用通配符*，需要明确指定允许的源
                .allowedOrigins("http://localhost:8080")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 暴露响应头，让前端可以访问额外的响应头信息
                .exposedHeaders("Content-Type", "X-Content-Type-Options", "Authorization")
                // 是否允许携带cookie
                .allowCredentials(true)
                // 预检请求的有效期（秒）
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加静态资源映射，使上传的图片可以通过URL访问
        // 将/api/files/images/**路径映射到uploads/images目录
        registry.addResourceHandler("/api/files/images/**")
                .addResourceLocations("file:uploads/images/");
    }
}

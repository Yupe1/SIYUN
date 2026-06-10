package com.yupe.siyun.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig  implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                // 线上直接放行所有源模式，允许任何公网 IP 带着端口访问
                .allowedOriginPatterns("*")
//              // 允许的前端源（生产环境建议指定具体域名，如"http://www.xxx.com"）
//                .allowedOriginPatterns(
//                        "http://*:5173",
//                        "http://127.0.0.1:5173",
//                        "http://localhost:5173",
//                        "http://*:5174",
//                        "http://127.0.0.1:5174",
//                        "http://localhost:5174"
//                )
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);


    }
}

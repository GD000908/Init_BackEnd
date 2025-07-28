// 예: GlobalCorsConfig.java
package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 모든 origin 허용 (개발/테스트용)
                .allowedOrigins(
                        "http://localhost:3000",
                        "https://init-front.vercel.app",
                        "https://init-front-git-main-parks-projects-52059b12.vercel.app",
                        "https://init-front-8vae2fth4-parks-projects-52059b12.vercel.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600L);
    }

}

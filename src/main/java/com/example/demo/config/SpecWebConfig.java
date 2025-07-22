package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class SpecWebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/spec/**")
                .allowedOrigins(
                        "https://init-front.vercel.app",
                        "https://init-front-git-main-parks-projects-52059b12.vercel.app",
                        "https://init-front-8vae2fth4-parks-projects-52059b12.vercel.app",
                        "http://localhost:3000",
                        "http://localhost:3001"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false); // false일 때만 "*" 허용됨
    }


    @Bean
    public CorsConfigurationSource specCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // allowCredentials가 false일 때는 "*" 패턴 사용 가능
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); 
        
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(false); // false로 변경
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(3600L); // preflight 캐시 시간
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/spec/**", configuration);
        return source;
    }
}

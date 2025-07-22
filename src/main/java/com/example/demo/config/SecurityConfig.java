package com.example.demo.config;

import com.example.demo.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/signup",
                                "/api/login",
                                "/api/check-userid/**",
                                "/api/check-email/**",
                                "/api/send-email-code",
                                "/api/verify-email-code",
                                "/api/find-userid",        // ✅ 아이디 찾기 API 추가
                                "/api/send-password-reset-code", // ✅ 비밀번호 재설정 관련 API 추가
                                "/api/verify-password-reset-code",
                                "/api/reset-password"
                        ).permitAll()
                        .requestMatchers("/api/cover-letters/**").permitAll()
                        .requestMatchers("/api/resumes/**").permitAll()
                        .requestMatchers("/api/job-calendar/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        .requestMatchers("/api/public-jobs/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureUrl("https://init-front.vercel.app/login?error=oauth2_failed")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ 로컬 개발과 배포 프론트 둘 다 허용
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",         // 로컬 프론트엔드
                "https://init-front.vercel.app"  // 배포 프론트엔드
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // ✅ 쿠키 포함 허용

        // ✅ Preflight 요청 캐싱 시간 늘림 (옵션)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

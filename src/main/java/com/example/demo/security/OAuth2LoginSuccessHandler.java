package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final com.example.demo.util.JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("🚀 OAuth2LoginSuccessHandler 실행 시작");

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            log.info("🔐 Google OAuth2 login success");
            log.info("🔍 OAuth2 User Attributes: {}", oAuth2User.getAttributes());

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String googleId = oAuth2User.getAttribute("sub");

            log.info("📧 추출된 정보: email={}, name={}, googleId={}", email, name, googleId);

            if (email == null || name == null) {
                log.error("❌ Google 계정 정보 부족: email={}, name={}", email, name);
                response.sendRedirect("https://init-front.vercel.app/login?error=invalid_google_account");
                return;
            }


            // 🔥 기존 사용자 확인 (자동 생성하지 않음)
            Optional<User> existingUser = userService.findByEmail(email);

            if (existingUser.isPresent()) {
                // 🎉 기존 회원 → 바로 로그인 처리
                User user = existingUser.get();

                // 계정 활성화 상태 확인
                if (!user.getIsActive()) {
                    log.error("❌ 비활성화된 계정: {}", email);
                    response.sendRedirect("https://init-front.vercel.app/login?error=account_disabled");

                    return;
                }

                log.info("✅ 기존 회원 로그인: userId={}, email={}", user.getUserId(), email);

                // JWT 토큰 생성
                String jwtToken = jwtUtil.generateToken(user.getUserId(), user.getId(), user.getRole().name());

                // 쿠키 설정
                setLoginCookies(response, user, jwtToken);

                // 로그인 완료 후 대시보드로 이동
                response.sendRedirect("https://init-front.vercel.app/login?googleLogin=success");


            } else {
                // 🆕 신규 사용자 → 회원가입 페이지로 안내
                log.info("🆕 신규 사용자 감지: email={}", email);

                // 구글 정보를 임시 쿠키에 저장 (회원가입 폼에서 사용)
                setTempGoogleInfoCookies(response, email, name, googleId);

                // 회원가입 페이지로 리다이렉트
                response.sendRedirect("https://init-front.vercel.app/login?signup=true&googleSignup=true");

            }

        } catch (Exception e) {
            log.error("❌ Google 로그인 처리 중 오류 발생", e);
            response.sendRedirect("https://init-front.vercel.app/login?error=google_login_failed");

        }

        log.info("🏁 OAuth2LoginSuccessHandler 실행 완료");
    }

    /**
     * 로그인 성공 시 쿠키 설정
     */
    private void setLoginCookies(HttpServletResponse response, User user, String jwtToken) {
        try {
            // authToken 쿠키
            Cookie tokenCookie = new Cookie("authToken", jwtToken);
            tokenCookie.setHttpOnly(false);
            tokenCookie.setSecure(false);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(tokenCookie);

            // userId 쿠키
            Cookie userIdCookie = new Cookie("userId", user.getId().toString());
            userIdCookie.setPath("/");
            userIdCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(userIdCookie);

            // userName 쿠키
            String encodedName = URLEncoder.encode(user.getName(), StandardCharsets.UTF_8);
            Cookie userNameCookie = new Cookie("userName", encodedName);
            userNameCookie.setPath("/");
            userNameCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(userNameCookie);

            // userRole 쿠키
            Cookie userRoleCookie = new Cookie("userRole", user.getRole().name());
            userRoleCookie.setPath("/");
            userRoleCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(userRoleCookie);

            log.info("✅ 로그인 쿠키 설정 완료");

        } catch (Exception e) {
            log.error("❌ 쿠키 설정 실패", e);
        }
    }

    /**
     * 신규 사용자용 임시 구글 정보 쿠키 설정
     */
    private void setTempGoogleInfoCookies(HttpServletResponse response, String email, String name, String googleId) {
        try {
            // 임시 구글 정보 쿠키 (회원가입 폼에서 사용, 10분 후 만료)
            Cookie tempEmailCookie = new Cookie("tempGoogleEmail", URLEncoder.encode(email, StandardCharsets.UTF_8));
            tempEmailCookie.setPath("/");
            tempEmailCookie.setMaxAge(10 * 60); // 10분
            response.addCookie(tempEmailCookie);

            Cookie tempNameCookie = new Cookie("tempGoogleName", URLEncoder.encode(name, StandardCharsets.UTF_8));
            tempNameCookie.setPath("/");
            tempNameCookie.setMaxAge(10 * 60); // 10분
            response.addCookie(tempNameCookie);

            if (googleId != null) {
                Cookie tempIdCookie = new Cookie("tempGoogleId", googleId);
                tempIdCookie.setPath("/");
                tempIdCookie.setMaxAge(10 * 60); // 10분
                response.addCookie(tempIdCookie);
            }

            log.info("✅ 임시 구글 정보 쿠키 설정 완료");

        } catch (Exception e) {
            log.error("❌ 임시 쿠키 설정 실패", e);
        }
    }
}
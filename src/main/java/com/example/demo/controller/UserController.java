package com.example.demo.controller;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.SignupDto;
import com.example.demo.service.EmailService;
import com.example.demo.service.EmailVerificationService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;

    /**
     * 회원가입을 처리하는 엔드포인트입니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupDto dto, HttpSession session,
                                         HttpServletRequest request, HttpServletResponse response) {
        String sessionId = session.getId();
        log.info("🚀 [SIGNUP] 회원가입 요청: userId={}, email={}, sessionId={}",
                dto.getUserId(), dto.getEmail(), sessionId);

        // 🔥 모바일 세션 설정
        setupMobileSession(request, response, session);

        try {
            // 이메일 인증 확인
            if (!emailVerificationService.isEmailVerified(dto.getEmail(), session)) {
                log.warn("❌ [SIGNUP] 이메일 인증 미완료: email={}, sessionId={}", dto.getEmail(), sessionId);
                return ResponseEntity.badRequest().body("이메일 인증을 완료해주세요.");
            }

            userService.signup(dto);

            // 🔥 성공 시 모바일 헤더 추가
            addMobileHeaders(response, session);

            log.info("✅ [SIGNUP] 회원가입 완료: userId={}, sessionId={}", dto.getUserId(), sessionId);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");

        } catch (IllegalArgumentException e) {
            log.error("❌ [SIGNUP] 회원가입 실패 (사용자 입력 오류): sessionId={}, error={}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ [SIGNUP] 회원가입 처리 중 예상치 못한 오류 발생: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().body("회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 로그인을 처리합니다.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto dto) {
        log.info("🔐 로그인 요청: userId={}", dto.getUserId());
        try {
            LoginResponseDto responseDto = userService.login(dto);
            log.info("✅ 로그인 성공: userId={}, role={}", responseDto.getUserId(), responseDto.getRole());
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            log.warn("❌ 로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("❌ 로그인 처리 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 아이디 중복을 확인합니다.
     */
    @GetMapping("/check-userid/{userId}")
    public ResponseEntity<Boolean> checkUserIdDuplicate(@PathVariable String userId) {
        log.info("🔍 아이디 중복 확인: {}", userId);
        try {
            boolean isDuplicate = userService.checkUserIdDuplicate(userId);
            log.info("✅ 아이디 중복 확인 결과: userId={}, isDuplicate={}", userId, isDuplicate);
            return ResponseEntity.ok(isDuplicate);
        } catch (Exception e) {
            log.error("❌ 아이디 중복 확인 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body(true);
        }
    }

    /**
     * 이메일 중복을 확인합니다.
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailDuplicate(@PathVariable String email) {
        log.info("📧 이메일 중복 확인: {}", email);
        try {
            boolean isDuplicate = userService.checkEmailDuplicate(email);
            log.info("✅ 이메일 중복 확인 결과: email={}, isDuplicate={}", email, isDuplicate);
            return ResponseEntity.ok(isDuplicate);
        } catch (Exception e) {
            log.error("❌ 이메일 중복 확인 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body(true);
        }
    }

    /**
     * 회원가입을 위한 이메일 인증 코드를 발송합니다.
     */
    @PostMapping("/send-email-code")
    public ResponseEntity<String> sendEmailCode(@RequestParam String email, HttpSession session,
                                                HttpServletRequest request, HttpServletResponse response) {
        String sessionId = session.getId();
        log.info("📩 [SEND] 이메일 인증 코드 발송 요청: email={}, sessionId={}", email, sessionId);

        // 🔥 요청 헤더 전체 로깅
        log.info("📋 [SEND] Request Headers:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            log.info("  {}: {}", headerName, request.getHeader(headerName));
        });

        // 🔥 기존 쿠키 확인
        if (request.getCookies() != null) {
            log.info("🍪 [SEND] Existing Cookies:");
            for (Cookie cookie : request.getCookies()) {
                log.info("  {}: {}", cookie.getName(), cookie.getValue());
            }
        }

        // 모바일 세션 설정
        setupMobileSession(request, response, session);

        try {
            String result = emailVerificationService.sendEmailVerificationCode(email, session);

            // 🔥 발송 후 세션 상태 확인
            log.info("✅ [SEND] 발송 완료 후 세션 확인:");
            log.info("  - SessionId: {}", session.getId());
            log.info("  - SessionNew: {}", session.isNew());
            log.info("  - EmailAuthCode: {}", session.getAttribute("emailAuthCode") != null ? "EXISTS" : "NULL");
            log.info("  - EmailAuthEmail: {}", session.getAttribute("emailAuthEmail"));
            log.info("  - EmailAuthTime: {}", session.getAttribute("emailAuthTime"));

            // 🔥 전역 저장소 상태도 로깅
            emailVerificationService.logGlobalStorageStatus();

            if ("success".equals(result)) {
                // 🔥 응답 헤더에 세션 ID 포함
                response.setHeader("X-Session-ID", session.getId());
                response.setHeader("X-Session-New", String.valueOf(session.isNew()));

                addMobileHeaders(response, session);
                log.info("✅ [SEND] 이메일 인증 코드 발송 성공: email={}, sessionId={}", email, sessionId);
                return ResponseEntity.ok("인증 코드가 발송되었습니다.");
            } else {
                log.warn("❌ [SEND] 이메일 인증 코드 발송 실패: email={}, sessionId={}, result={}", email, sessionId, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ [SEND] 이메일 인증 코드 발송 중 오류: email={}, sessionId={}", email, sessionId, e);
            return ResponseEntity.internalServerError().body("이메일 발송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 🔥 회원가입을 위한 이메일 인증 코드를 검증합니다. (전역 저장소 대응)
     */
    @PostMapping("/verify-email-code")
    public ResponseEntity<String> verifyEmailCode(@RequestParam String email, @RequestParam String code,
                                                  HttpSession session, HttpServletRequest request,
                                                  HttpServletResponse response) {
        String sessionId = session.getId();
        log.info("🔐 [VERIFY] 이메일 인증 코드 검증 요청: email={}, sessionId={}", email, sessionId);

        // 🔥 헤더에서 클라이언트 세션 ID 확인
        String clientSessionId = request.getHeader("X-Session-ID");
        String allClientSessionIds = request.getHeader("X-All-Session-IDS");

        log.info("🔍 [VERIFY] 클라이언트 세션 정보:");
        log.info("  - 클라이언트 세션 ID: {}", clientSessionId);
        log.info("  - 모든 클라이언트 세션 IDs: {}", allClientSessionIds);
        log.info("  - 서버 세션 ID: {}", sessionId);

        // 🔥 요청 헤더 전체 로깅
        log.info("📋 [VERIFY] Request Headers:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            log.info("  {}: {}", headerName, request.getHeader(headerName));
        });

        // 🔥 쿠키 상세 확인
        if (request.getCookies() != null) {
            log.info("🍪 [VERIFY] Cookies:");
            for (Cookie cookie : request.getCookies()) {
                log.info("  {}: {} (domain: {}, path: {}, secure: {})",
                        cookie.getName(), cookie.getValue(),
                        cookie.getDomain(), cookie.getPath(), cookie.getSecure());
            }
        } else {
            log.warn("⚠️ [VERIFY] No cookies received!");
        }

        // 🔥 세션 속성 전체 확인
        log.info("🔍 [VERIFY] Session Attributes:");
        session.getAttributeNames().asIterator().forEachRemaining(attrName -> {
            Object value = session.getAttribute(attrName);
            log.info("  {}: {}", attrName, value);
        });

        // 🔥 세션 데이터 상세 확인
        Object savedCode = session.getAttribute("emailAuthCode");
        Object savedEmail = session.getAttribute("emailAuthEmail");
        Object savedTime = session.getAttribute("emailAuthTime");

        log.info("🔍 [VERIFY] 세션 데이터 상세:");
        log.info("  - emailAuthCode: {}", savedCode != null ? "EXISTS(" + savedCode + ")" : "NULL");
        log.info("  - emailAuthEmail: {}", savedEmail);
        log.info("  - emailAuthTime: {}", savedTime);
        log.info("  - Session isNew: {}", session.isNew());

        if (savedTime != null) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - (Long) savedTime;
            log.info("🔍 [VERIFY] 경과 시간: {}ms ({}초)", elapsedTime, elapsedTime / 1000);
        }

        // 🔥 전역 저장소 상태 로깅
        emailVerificationService.logGlobalStorageStatus();

        setupMobileSession(request, response, session);

        try {
            String result = emailVerificationService.verifyEmailCode(email, code, session);

            if ("success".equals(result)) {
                // 🔥 성공 시 세션 정보 응답 헤더에 추가
                response.setHeader("X-Session-ID", session.getId());
                response.setHeader("X-Verification-Success", "true");
                response.setHeader("X-Verification-Method", "GLOBAL_OR_SESSION");

                log.info("✅ [VERIFY] 인증 성공: email={}, sessionId={}", email, sessionId);
                addMobileHeaders(response, session);
                return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
            } else {
                log.warn("❌ [VERIFY] 인증 실패: email={}, sessionId={}, result={}", email, sessionId, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ [VERIFY] 인증 검증 중 오류: email={}, sessionId={}", email, sessionId, e);
            return ResponseEntity.internalServerError().body("인증 처리 중 오류가 발생했습니다.");
        }
    }

    // =================================================================
    // == 아이디 찾기 및 비밀번호 재설정 API
    // =================================================================

    /**
     * 이메일로 가입된 아이디를 찾습니다.
     */
    @PostMapping("/find-userid")
    public ResponseEntity<String> findUserId(@RequestParam String email) {
        log.info("🔍 아이디 찾기 요청: email={}", email);
        try {
            String userId = userService.findUserIdByEmail(email);
            log.info("✅ 아이디 찾기 성공: email={}, userId={}", email, userId);
            return ResponseEntity.ok(userId);
        } catch (IllegalArgumentException e) {
            log.warn("❌ 아이디 찾기 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 아이디 찾기 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body("아이디 찾기 중 오류가 발생했습니다.");
        }
    }

    /**
     * 🔥 비밀번호 재설정을 위한 이메일 인증 코드를 발송합니다.
     */
    @PostMapping("/send-password-reset-code")
    public ResponseEntity<String> sendPasswordResetCode(@RequestParam String userId, @RequestParam String email,
                                                        HttpSession session, HttpServletRequest request,
                                                        HttpServletResponse response) {
        String sessionId = session.getId();
        log.info("🔐 [PWD_RESET_SEND] 비밀번호 재설정 인증 코드 발송 요청: userId={}, email={}, sessionId={}",
                userId, email, sessionId);

        // 🔥 모바일 세션 설정
        setupMobileSession(request, response, session);

        try {
            String result = userService.sendPasswordResetCode(userId, email, session);

            // 🔥 전역 저장소 상태 로깅
            userService.logPasswordResetStorageStatus();

            if ("success".equals(result)) {
                addMobileHeaders(response, session);
                log.info("✅ [PWD_RESET_SEND] 비밀번호 재설정 코드 발송 성공: userId={}, sessionId={}", userId, sessionId);
                return ResponseEntity.ok("비밀번호 재설정을 위한 인증 코드가 발송되었습니다.");
            } else {
                log.warn("❌ [PWD_RESET_SEND] 비밀번호 재설정 코드 발송 실패: userId={}, sessionId={}, result={}",
                        userId, sessionId, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ [PWD_RESET_SEND] 비밀번호 재설정 코드 발송 중 오류: userId={}, sessionId={}", userId, sessionId, e);
            return ResponseEntity.internalServerError().body("인증 코드 발송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 🔥 비밀번호 재설정을 위한 인증 코드를 검증합니다.
     */
    @PostMapping("/verify-password-reset-code")
    public ResponseEntity<String> verifyPasswordResetCode(@RequestParam String userId, @RequestParam String email,
                                                          @RequestParam String code, HttpSession session,
                                                          HttpServletRequest request, HttpServletResponse response) {
        String sessionId = session.getId();
        log.info("🔐 [PWD_RESET_VERIFY] 비밀번호 재설정 인증 코드 검증 요청: userId={}, email={}, sessionId={}",
                userId, email, sessionId);

        setupMobileSession(request, response, session);

        try {
            String result = userService.verifyPasswordResetCode(userId, email, code, session);

            // 🔥 전역 저장소 상태 로깅
            userService.logPasswordResetStorageStatus();

            if ("success".equals(result)) {
                addMobileHeaders(response, session);
                log.info("✅ [PWD_RESET_VERIFY] 비밀번호 재설정 인증 코드 검증 성공: userId={}, sessionId={}", userId, sessionId);
                return ResponseEntity.ok("인증이 완료되었습니다. 새 비밀번호를 설정해주세요.");
            } else {
                log.warn("❌ [PWD_RESET_VERIFY] 비밀번호 재설정 인증 코드 검증 실패: userId={}, sessionId={}, result={}",
                        userId, sessionId, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ [PWD_RESET_VERIFY] 비밀번호 재설정 인증 코드 검증 중 오류: userId={}, sessionId={}", userId, sessionId, e);
            return ResponseEntity.internalServerError().body("인증 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 🔥 인증 완료 후, 새 비밀번호로 재설정합니다.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String userId, @RequestParam String email,
                                                @RequestParam String newPassword, HttpSession session,
                                                HttpServletRequest request, HttpServletResponse response) {
        String sessionId = session.getId();
        log.info("🔐 [PWD_RESET] 비밀번호 재설정 요청: userId={}, email={}, sessionId={}", userId, email, sessionId);

        setupMobileSession(request, response, session);

        try {
            String result = userService.resetPassword(userId, email, newPassword, session);

            if ("success".equals(result)) {
                addMobileHeaders(response, session);
                log.info("✅ [PWD_RESET] 비밀번호 재설정 성공: userId={}, sessionId={}", userId, sessionId);
                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                log.warn("❌ [PWD_RESET] 비밀번호 재설정 실패: userId={}, sessionId={}, result={}", userId, sessionId, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ [PWD_RESET] 비밀번호 재설정 중 오류: userId={}, sessionId={}", userId, sessionId, e);
            return ResponseEntity.internalServerError().body("비밀번호 재설정 중 오류가 발생했습니다.");
        }
    }

    // =================================================================
    // == 🔥 테스트 및 디버깅 API (개발용)
    // =================================================================

    /**
     * 🔥 이메일 서비스 연결 테스트
     */
    @GetMapping("/test-email")
    public ResponseEntity<String> testEmailConnection() {
        log.info("🧪 [TEST] 이메일 서비스 연결 테스트 요청");
        try {
            boolean isConnected = emailService.testEmailConnection();
            if (isConnected) {
                return ResponseEntity.ok("이메일 서비스 연결이 정상입니다.");
            } else {
                return ResponseEntity.internalServerError().body("이메일 서비스 연결에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("❌ [TEST] 이메일 연결 테스트 실패", e);
            return ResponseEntity.internalServerError().body("이메일 연결 테스트 중 오류: " + e.getMessage());
        }
    }

    /**
     * 🔥 세션 상태 확인 API (디버깅용)
     */
    @GetMapping("/session-status")
    public ResponseEntity<String> getSessionStatus(HttpSession session, HttpServletRequest request) {
        log.info("🔍 [DEBUG] 세션 상태 확인 요청");
        
        StringBuilder status = new StringBuilder();
        status.append("=== 세션 상태 ===\n");
        status.append("Session ID: ").append(session.getId()).append("\n");
        status.append("Session New: ").append(session.isNew()).append("\n");
        status.append("Max Inactive Interval: ").append(session.getMaxInactiveInterval()).append("초\n");
        status.append("Creation Time: ").append(new java.util.Date(session.getCreationTime())).append("\n");
        status.append("Last Accessed Time: ").append(new java.util.Date(session.getLastAccessedTime())).append("\n");
        
        status.append("\n=== 세션 속성 ===\n");
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            Object value = session.getAttribute(name);
            status.append(name).append(": ").append(value).append("\n");
        }
        
        status.append("\n=== 요청 헤더 ===\n");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            status.append(name).append(": ").append(value).append("\n");
        }
        
        status.append("\n=== 쿠키 ===\n");
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                status.append(cookie.getName()).append("=").append(cookie.getValue()).append("\n");
            }
        } else {
            status.append("쿠키 없음\n");
        }
        
        return ResponseEntity.ok(status.toString());
    }

    // =================================================================
    // == 🔥 모바일 세션 처리 유틸리티 메서드들
    // =================================================================

    /**
     * 🔥 모바일 세션 설정 강화 (Railway HTTPS 환경 대응)
     */
    private void setupMobileSession(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String userAgent = request.getHeader("User-Agent");
        String sessionId = session.getId();
        String domain = request.getServerName();

        log.info("🔧 [SETUP] 세션 설정: sessionId={}, domain={}", sessionId.substring(0, 8), domain);
        log.info("🔧 [SETUP] userAgent={}", userAgent);

        // 모바일 환경 감지
        boolean isMobile = userAgent != null && (
                userAgent.contains("Mobile") || userAgent.contains("iPhone") ||
                        userAgent.contains("Android") || userAgent.contains("iPad") ||
                        userAgent.contains("webOS") || userAgent.contains("BlackBerry") ||
                        userAgent.contains("Windows Phone")
        );

        // 🔥 Railway 환경 감지
        boolean isRailway = domain.contains("railway.app") ||
                request.getHeader("x-railway-edge") != null;

        log.info("📱 [SETUP] isMobile: {}, isRailway: {}", isMobile, isRailway);

        // 🔥 기존 JSESSIONID 쿠키 확인
        String existingSessionId = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    existingSessionId = cookie.getValue();
                    break;
                }
            }
        }

        log.info("🔍 [SETUP] 기존 세션 ID: {}, 현재 세션 ID: {}",
                existingSessionId != null ? existingSessionId.substring(0, 8) : "null",
                sessionId.substring(0, 8));

        // 🔥 모바일이거나 Railway 환경에서 강화된 쿠키 설정
        if (isMobile || isRailway) {

            // 🔥 모든 조합의 쿠키 설정 (브라우저별 대응)
            String[] cookieSettings = {
                    // 1. HTTPS + SameSite=None (크로스 사이트 허용)
                    String.format("JSESSIONID=%s; Path=/; SameSite=None; Secure; HttpOnly=false", sessionId),

                    // 2. HTTPS + SameSite=Lax (일반적인 설정)
                    String.format("JSESSIONID=%s; Path=/; SameSite=Lax; Secure; HttpOnly=false", sessionId),

                    // 3. 기본 설정 (호환성)
                    String.format("JSESSIONID=%s; Path=/; Secure; HttpOnly=false", sessionId),

                    // 4. 도메인 없는 설정
                    String.format("JSESSIONID=%s; Path=/; HttpOnly=false", sessionId),

                    // 5. 추가 백업용 쿠키 (다른 이름)
                    String.format("SESSIONID=%s; Path=/; SameSite=None; Secure; HttpOnly=false", sessionId),
                    String.format("SESSID=%s; Path=/; SameSite=Lax; Secure; HttpOnly=false", sessionId)
            };

            for (String cookieSetting : cookieSettings) {
                response.addHeader("Set-Cookie", cookieSetting);
                log.info("🍪 [SETUP] 쿠키 설정: {}", cookieSetting);
            }

            // 🔥 세션 타임아웃 연장 (30분)
            session.setMaxInactiveInterval(1800); // 30분

            // 🔥 세션 강제 커밋 (일부 환경에서 필요)
            try {
                session.setAttribute("sessionCommit", System.currentTimeMillis());
                log.info("🔥 [SETUP] 세션 강제 커밋 완료");
            } catch (Exception e) {
                log.warn("⚠️ [SETUP] 세션 커밋 실패: {}", e.getMessage());
            }

            log.info("⏰ [SETUP] 세션 타임아웃 30분으로 설정");
        }
    }

    /**
     * 🔥 모바일용 응답 헤더 추가
     */
    private void addMobileHeaders(HttpServletResponse response, HttpSession session) {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Expose-Headers", "X-Session-ID, X-Session-New, X-Verification-Success, X-Verification-Method");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setHeader("X-Session-ID", session.getId());

        log.info("📤 [HEADERS] 모바일 응답 헤더 설정 완료: sessionId={}", session.getId().substring(0, 8));
    }
}
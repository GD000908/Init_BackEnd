// EmailVerificationService.java - 전역 저장소 추가

package com.example.demo.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailService emailService;
    private final UserService userService;

    // 🔥 전역 인증 데이터 저장소 (메모리 기반)
    private static final ConcurrentHashMap<String, EmailAuthData> globalAuthStorage = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService cleanupService = Executors.newScheduledThreadPool(1);

    // 🔥 정리 작업 스케줄러 (1분마다 만료된 데이터 삭제)
    static {
        cleanupService.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            globalAuthStorage.entrySet().removeIf(entry -> {
                boolean isExpired = currentTime - entry.getValue().getTimestamp() > 1800000; // 30분
                if (isExpired) {
                    log.info("🧹 [CLEANUP] 만료된 인증 데이터 삭제: email={}", entry.getKey());
                }
                return isExpired;
            });
        }, 1, 1, TimeUnit.MINUTES);
    }

    // 🔥 인증 데이터 저장 클래스
    private static class EmailAuthData {
        private final String email;
        private final String code;
        private final String sessionId;
        private final long timestamp;
        private boolean verified;

        public EmailAuthData(String email, String code, String sessionId, long timestamp) {
            this.email = email;
            this.code = code;
            this.sessionId = sessionId;
            this.timestamp = timestamp;
            this.verified = false;
        }

        // Getters
        public String getEmail() { return email; }
        public String getCode() { return code; }
        public String getSessionId() { return sessionId; }
        public long getTimestamp() { return timestamp; }
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
    }

    /**
     * 이메일 인증 코드 발송 (전역 저장소 사용)
     */
    public String sendEmailVerificationCode(String email, HttpSession session) {
        try {
            // 1. 이메일 형식 검증
            if (email == null || !email.contains("@")) {
                return "올바른 이메일 주소를 입력해주세요.";
            }

            // 2. 이메일 중복 확인
            if (userService.checkEmailDuplicate(email)) {
                return "이미 가입된 이메일입니다.";
            }

            // 3. 인증 코드 생성 및 발송
            String authCode = emailService.createAuthCode();
            emailService.sendEmailAuthCode(email, authCode);

            String sessionId = session.getId();
            long currentTime = System.currentTimeMillis();

            // 4. 🔥 기존 세션 저장 (기본)
            session.setAttribute("emailAuthCode", authCode);
            session.setAttribute("emailAuthEmail", email);
            session.setAttribute("emailAuthTime", currentTime);
            session.setMaxInactiveInterval(1800); // 30분

            // 5. 🔥 전역 저장소에도 저장 (모바일 백업용)
            EmailAuthData authData = new EmailAuthData(email, authCode, sessionId, currentTime);
            globalAuthStorage.put(email, authData);

            log.info("🔥 [GLOBAL] 전역 저장소에 인증 데이터 저장: email={}, sessionId={}",
                    email, sessionId.substring(0, 8));

            // 🔥 세션 강제 저장
            try {
                session.setAttribute("sessionForced", "true");
                log.info("🔥 세션 강제 저장 완료: sessionId={}", sessionId);
            } catch (Exception e) {
                log.warn("⚠️ 세션 강제 저장 실패: {}", e.getMessage());
            }

            log.info("✅ 이메일 인증 코드 발송 및 저장 완료: email={}, sessionId={}",
                    email, sessionId);
            return "success";

        } catch (Exception e) {
            log.error("❌ 이메일 인증 코드 발송 실패: email={}, sessionId={}, 오류: {}",
                    email, session.getId(), e.getMessage());
            return "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    /**
     * 🔥 전역 저장소에서 이메일 인증 코드 검증
     */
    public String verifyEmailCodeGlobal(String email, String code) {
        try {
            log.info("🔐 [GLOBAL] 전역 저장소에서 인증 코드 검증: email={}", email);

            EmailAuthData authData = globalAuthStorage.get(email);

            if (authData == null) {
                log.warn("❌ [GLOBAL] 전역 저장소에 데이터 없음: email={}", email);
                return "인증 코드가 만료되었습니다. 다시 요청해주세요.";
            }

            // 1. 만료 시간 확인 (30분)
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - authData.getTimestamp();

            if (elapsedTime > 1800000) { // 30분
                globalAuthStorage.remove(email);
                log.warn("❌ [GLOBAL] 인증 코드 만료: email={}, elapsed={}ms", email, elapsedTime);
                return "인증 코드가 만료되었습니다. 다시 요청해주세요.";
            }

            // 2. 인증 코드 확인
            if (!authData.getCode().equals(code)) {
                log.warn("❌ [GLOBAL] 인증 코드 불일치: email={}", email);
                return "인증 코드가 올바르지 않습니다.";
            }

            // 3. 인증 성공
            authData.setVerified(true);
            log.info("✅ [GLOBAL] 전역 저장소 인증 성공: email={}, sessionId={}",
                    email, authData.getSessionId().substring(0, 8));

            return "success";

        } catch (Exception e) {
            log.error("❌ [GLOBAL] 전역 저장소 인증 검증 실패: email={}", email, e);
            return "인증 처리 중 오류가 발생했습니다.";
        }
    }

    /**
     * 이메일 인증 코드 검증 - 개선된 버전 (세션 + 전역 저장소)
     */
    public String verifyEmailCode(String email, String code, HttpSession session) {
        try {
            log.info("🔐 인증 코드 검증 시작: email={}, sessionId={}", email, session.getId());

            String sessionId = session.getId();
            String savedCode = (String) session.getAttribute("emailAuthCode");
            String savedEmail = (String) session.getAttribute("emailAuthEmail");
            Long savedTime = (Long) session.getAttribute("emailAuthTime");

            // 🔥 디버깅 로그 강화
            log.info("🔍 세션 데이터 확인:");
            log.info("  - savedCode: {}", savedCode != null ? "EXISTS" : "NULL");
            log.info("  - savedEmail: {}", savedEmail);
            log.info("  - savedTime: {}", savedTime);
            log.info("  - inputEmail: {}", email);
            log.info("  - inputCode: {}", code != null ? "PROVIDED" : "NULL");

            // 🔥 세션 데이터가 없다면 전역 저장소에서 시도
            if (savedCode == null || savedEmail == null || savedTime == null) {
                log.warn("⚠️ 세션 데이터 없음, 전역 저장소에서 검증 시도");
                return verifyEmailCodeGlobal(email, code);
            }

            // 기존 세션 기반 검증 로직
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - savedTime;
            long timeoutMs = 1800000; // 30분

            log.info("🔍 시간 확인: elapsed={}ms, timeout={}ms, remaining={}ms",
                    elapsedTime, timeoutMs, timeoutMs - elapsedTime);

            if (elapsedTime > timeoutMs) {
                clearEmailVerificationSession(session);
                log.warn("❌ 세션 인증 코드 만료, 전역 저장소에서 재시도");
                return verifyEmailCodeGlobal(email, code);
            }

            if (!savedEmail.equals(email)) {
                log.warn("❌ 이메일 불일치: saved={}, input={}", savedEmail, email);
                return "인증을 요청한 이메일과 일치하지 않습니다.";
            }

            if (!savedCode.equals(code)) {
                log.warn("❌ 인증 코드 불일치");
                return "인증 코드가 올바르지 않습니다.";
            }

            // 인증 성공
            session.setAttribute("emailVerified", true);
            session.setAttribute("verifiedEmail", email);
            session.setAttribute("emailVerifiedTime", System.currentTimeMillis());
            session.setMaxInactiveInterval(1800); // 30분

            // 🔥 전역 저장소도 업데이트
            EmailAuthData authData = globalAuthStorage.get(email);
            if (authData != null) {
                authData.setVerified(true);
            }

            try {
                session.setAttribute("verificationForced", "true");
                log.info("🔥 인증 완료 세션 강제 저장: sessionId={}", sessionId);
            } catch (Exception e) {
                log.warn("⚠️ 인증 완료 세션 저장 실패: {}", e.getMessage());
            }

            log.info("✅ 이메일 인증 성공: email={}, sessionId={}", email, sessionId);
            return "success";

        } catch (Exception e) {
            log.error("❌ 이메일 인증 검증 실패: email={}, sessionId={}, 오류: {}",
                    email, session.getId(), e.getMessage());
            return "인증 처리 중 오류가 발생했습니다.";
        }
    }

    /**
     * 이메일 인증 완료 여부 확인 - 개선된 버전
     */
    public boolean isEmailVerified(String email, HttpSession session) {
        try {
            Boolean isVerified = (Boolean) session.getAttribute("emailVerified");
            String verifiedEmail = (String) session.getAttribute("verifiedEmail");
            Long verifiedTime = (Long) session.getAttribute("emailVerifiedTime");

            log.info("🔍 인증 상태 확인: email={}, sessionId={}", email, session.getId());
            log.info("  - isVerified: {}", isVerified);
            log.info("  - verifiedEmail: {}", verifiedEmail);
            log.info("  - verifiedTime: {}", verifiedTime);

            // 기본 세션 검증
            if (isVerified != null && isVerified && email.equals(verifiedEmail)) {
                if (verifiedTime != null) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - verifiedTime;
                    if (elapsedTime <= 1800000) { // 30분 이내
                        log.info("✅ 세션 기반 인증 상태 확인 성공");
                        return true;
                    }
                }
            }

            // 🔥 전역 저장소에서도 확인
            EmailAuthData authData = globalAuthStorage.get(email);
            if (authData != null && authData.isVerified()) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - authData.getTimestamp();
                if (elapsedTime <= 1800000) { // 30분 이내
                    log.info("✅ 전역 저장소 기반 인증 상태 확인 성공");

                    // 🔥 세션에도 인증 정보 복사
                    session.setAttribute("emailVerified", true);
                    session.setAttribute("verifiedEmail", email);
                    session.setAttribute("emailVerifiedTime", currentTime);

                    return true;
                }
            }

            log.warn("❌ 인증 상태 확인 실패");
            return false;

        } catch (Exception e) {
            log.error("❌ 인증 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 🔥 이메일 인증 관련 세션 데이터 정리
     */
    private void clearEmailVerificationSession(HttpSession session) {
        try {
            session.removeAttribute("emailAuthCode");
            session.removeAttribute("emailAuthEmail");
            session.removeAttribute("emailAuthTime");
            session.removeAttribute("emailVerified");
            session.removeAttribute("verifiedEmail");
            session.removeAttribute("emailVerifiedTime");
            log.info("🧹 이메일 인증 세션 데이터 정리 완료");
        } catch (Exception e) {
            log.warn("⚠️ 세션 데이터 정리 실패: {}", e.getMessage());
        }
    }

    /**
     * 🔥 전역 저장소 상태 확인 (디버깅용)
     */
    public void logGlobalStorageStatus() {
        log.info("📊 [GLOBAL] 전역 저장소 상태: 총 {}개 항목", globalAuthStorage.size());
        globalAuthStorage.forEach((email, authData) -> {
            log.info("  - {}: sessionId={}, verified={}, age={}분",
                    email,
                    authData.getSessionId().substring(0, 8),
                    authData.isVerified(),
                    (System.currentTimeMillis() - authData.getTimestamp()) / 60000);
        });
    }
}
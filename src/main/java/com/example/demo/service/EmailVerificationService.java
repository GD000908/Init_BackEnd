package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailService emailService;
    private final UserService userService;

    /**
     * 이메일 인증 코드 발송 (중복확인 포함)
     * @param email 이메일 주소
     * @param session HTTP 세션
     * @return 성공 여부 메시지
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

            // 4. 세션에 인증 코드와 이메일 저장 (5분 후 만료)
            session.setAttribute("emailAuthCode", authCode);
            session.setAttribute("emailAuthEmail", email);
            session.setAttribute("emailAuthTime", System.currentTimeMillis());
            session.setMaxInactiveInterval(300); // 5분

            log.info("이메일 인증 코드 발송 및 세션 저장 완료: {}", email);
            return "success";

        } catch (Exception e) {
            log.error("이메일 인증 코드 발송 실패: {}, 오류: {}", email, e.getMessage());
            return "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    /**
     * 이메일 인증 코드 검증
     * @param email 이메일 주소
     * @param code 인증 코드
     * @param session HTTP 세션
     * @return 검증 결과
     */
    public String verifyEmailCode(String email, String code, HttpSession session) {
        try {
            String savedCode = (String) session.getAttribute("emailAuthCode");
            String savedEmail = (String) session.getAttribute("emailAuthEmail");
            Long savedTime = (Long) session.getAttribute("emailAuthTime");

            // 1. 세션에 저장된 데이터 확인
            if (savedCode == null || savedEmail == null || savedTime == null) {
                return "인증 코드가 만료되었습니다. 다시 요청해주세요.";
            }

            // 2. 5분 만료 확인
            long currentTime = System.currentTimeMillis();
            if (currentTime - savedTime > 300000) { // 5분 = 300,000ms
                // 만료된 세션 데이터 삭제
                session.removeAttribute("emailAuthCode");
                session.removeAttribute("emailAuthEmail");
                session.removeAttribute("emailAuthTime");
                return "인증 코드가 만료되었습니다. 다시 요청해주세요.";
            }

            // 3. 이메일 주소 일치 확인
            if (!savedEmail.equals(email)) {
                return "인증을 요청한 이메일과 일치하지 않습니다.";
            }

            // 4. 인증 코드 일치 확인
            if (!savedCode.equals(code)) {
                return "인증 코드가 올바르지 않습니다.";
            }

            // 5. 인증 성공 - 인증 완료 표시를 위한 세션 설정
            session.setAttribute("emailVerified", true);
            session.setAttribute("verifiedEmail", email);

            log.info("이메일 인증 성공: {}", email);
            return "success";

        } catch (Exception e) {
            log.error("이메일 인증 검증 실패: {}, 오류: {}", email, e.getMessage());
            return "인증 처리 중 오류가 발생했습니다.";
        }
    }

    /**
     * 이메일 인증 완료 여부 확인
     * @param email 이메일 주소
     * @param session HTTP 세션
     * @return 인증 완료 여부
     */
    public boolean isEmailVerified(String email, HttpSession session) {
        Boolean isVerified = (Boolean) session.getAttribute("emailVerified");
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");

        return isVerified != null && isVerified && email.equals(verifiedEmail);
    }
}
package com.example.demo.controller;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.SignupDto;
import com.example.demo.service.EmailVerificationService;
import com.example.demo.service.UserService;
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

    /**
     * 회원가입을 처리하는 엔드포인트입니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupDto dto, HttpSession session) {
        log.info("🚀 회원가입 요청: userId={}, email={}", dto.getUserId(), dto.getEmail());

        try {
            // 이메일 인증 확인
            if (!emailVerificationService.isEmailVerified(dto.getEmail(), session)) {
                log.warn("❌ 이메일 인증 미완료: {}", dto.getEmail());
                return ResponseEntity.badRequest().body("이메일 인증을 완료해주세요.");
            }

            userService.signup(dto);
            log.info("✅ 회원가입 완료: userId={}", dto.getUserId());
            return ResponseEntity.ok("회원가입이 완료되었습니다.");

        } catch (IllegalArgumentException e) {
            log.error("❌ 회원가입 실패 (사용자 입력 오류): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 회원가입 처리 중 예상치 못한 오류 발생", e);
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
    public ResponseEntity<String> sendEmailCode(@RequestParam String email, HttpSession session) {
        log.info("📩 이메일 인증 코드 발송 요청: {}", email);
        try {
            String result = emailVerificationService.sendEmailVerificationCode(email, session);
            if ("success".equals(result)) {
                log.info("✅ 이메일 인증 코드 발송 성공: {}", email);
                return ResponseEntity.ok("인증 코드가 발송되었습니다.");
            } else {
                log.warn("❌ 이메일 인증 코드 발송 실패: email={}, result={}", email, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ 이메일 인증 코드 발송 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body("이메일 발송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 회원가입을 위한 이메일 인증 코드를 검증합니다.
     */
    @PostMapping("/verify-email-code")
    public ResponseEntity<String> verifyEmailCode(@RequestParam String email, @RequestParam String code, HttpSession session) {
        log.info("🔐 이메일 인증 코드 검증 요청: email={}, code={}", email, "***");
        try {
            String result = emailVerificationService.verifyEmailCode(email, code, session);
            if ("success".equals(result)) {
                log.info("✅ 이메일 인증 코드 검증 성공: {}", email);
                return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
            } else {
                log.warn("❌ 이메일 인증 코드 검증 실패: email={}, result={}", email, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ 이메일 인증 코드 검증 중 예상치 못한 오류 발생", e);
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
     * 비밀번호 재설정을 위한 이메일 인증 코드를 발송합니다.
     */
    @PostMapping("/send-password-reset-code")
    public ResponseEntity<String> sendPasswordResetCode(@RequestParam String userId, @RequestParam String email, HttpSession session) {
        log.info("🔐 비밀번호 재설정 인증 코드 발송 요청: userId={}, email={}", userId, email);
        try {
            String result = userService.sendPasswordResetCode(userId, email, session);
            if ("success".equals(result)) {
                log.info("✅ 비밀번호 재설정 코드 발송 성공: userId={}", userId);
                return ResponseEntity.ok("비밀번호 재설정을 위한 인증 코드가 발송되었습니다.");
            } else {
                log.warn("❌ 비밀번호 재설정 코드 발송 실패: userId={}, result={}", userId, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ 비밀번호 재설정 코드 발송 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body("인증 코드 발송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀번호 재설정을 위한 인증 코드를 검증합니다.
     */
    @PostMapping("/verify-password-reset-code")
    public ResponseEntity<String> verifyPasswordResetCode(@RequestParam String userId, @RequestParam String email, @RequestParam String code, HttpSession session) {
        log.info("🔐 비밀번호 재설정 인증 코드 검증 요청: userId={}, email={}", userId, email);
        try {
            String result = userService.verifyPasswordResetCode(userId, email, code, session);
            if ("success".equals(result)) {
                log.info("✅ 비밀번호 재설정 인증 코드 검증 성공: userId={}", userId);
                return ResponseEntity.ok("인증이 완료되었습니다. 새 비밀번호를 설정해주세요.");
            } else {
                log.warn("❌ 비밀번호 재설정 인증 코드 검증 실패: userId={}, result={}", userId, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ 비밀번호 재설정 인증 코드 검증 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body("인증 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 인증 완료 후, 새 비밀번호로 재설정합니다.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String userId, @RequestParam String email, @RequestParam String newPassword, HttpSession session) {
        log.info("🔐 비밀번호 재설정 요청: userId={}, email={}", userId, email);
        try {
            String result = userService.resetPassword(userId, email, newPassword, session);
            if ("success".equals(result)) {
                log.info("✅ 비밀번호 재설정 성공: userId={}", userId);
                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                log.warn("❌ 비밀번호 재설정 실패: userId={}, result={}", userId, result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("❌ 비밀번호 재설정 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body("비밀번호 재설정 중 오류가 발생했습니다.");
        }
    }
}
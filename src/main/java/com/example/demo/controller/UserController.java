package com.example.demo.controller;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.SignupDto;
import com.example.demo.service.UserService;
import com.example.demo.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
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
        // 🔥 세션 기반 이메일 인증 완료 여부 확인
        if (!emailVerificationService.isEmailVerified(dto.getEmail(), session)) {
            return ResponseEntity.badRequest().body("이메일 인증을 완료해주세요.");
        }

        userService.signup(dto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인을 처리하는 엔드포인트입니다.
     * 성공 시, 사용자 정보가 담긴 DTO를 응답 본문에 포함하여 반환합니다.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto dto) {
        LoginResponseDto responseDto = userService.login(dto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 아이디 중복 확인 API
     */
    @GetMapping("/check-userid/{userId}")
    public ResponseEntity<Boolean> checkUserIdDuplicate(@PathVariable String userId) {
        boolean isDuplicate = userService.checkUserIdDuplicate(userId);
        return ResponseEntity.ok(isDuplicate);
    }

    /**
     * 이메일 중복 확인 API
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailDuplicate(@PathVariable String email) {
        boolean isDuplicate = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok(isDuplicate);
    }

    /**
     * 🆕 이메일 인증 코드 발송 API (중복확인 포함)
     */
    @PostMapping("/send-email-code")
    public ResponseEntity<String> sendEmailCode(@RequestParam String email, HttpSession session) {
        String result = emailVerificationService.sendEmailVerificationCode(email, session);

        if ("success".equals(result)) {
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 🆕 이메일 인증 코드 검증 API
     */
    @PostMapping("/verify-email-code")
    public ResponseEntity<String> verifyEmailCode(
            @RequestParam String email,
            @RequestParam String code,
            HttpSession session) {

        String result = emailVerificationService.verifyEmailCode(email, code, session);

        if ("success".equals(result)) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
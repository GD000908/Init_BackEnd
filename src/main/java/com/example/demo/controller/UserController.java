package com.example.demo.controller;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.SignupDto;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입을 처리하는 엔드포인트입니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupDto dto) {
        try {
            userService.signup(dto);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 로그인을 처리하는 엔드포인트입니다.
     * 성공 시, 사용자 정보가 담긴 DTO를 응답 본문에 포함하여 반환합니다.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        try {
            // 1. 서비스에서 LoginResponseDto 객체를 받아옵니다.
            LoginResponseDto responseDto = userService.login(dto);

            // 2. 받아온 DTO 객체를 ResponseEntity.ok()에 담아 클라이언트로 전달합니다.
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            // 로그인 실패 시 에러 메시지를 JSON으로 반환
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 🆕 아이디 중복 확인 API
     */
    @GetMapping("/check-userid/{userId}")
    public ResponseEntity<Boolean> checkUserIdDuplicate(@PathVariable String userId) {
        boolean isDuplicate = userService.checkUserIdDuplicate(userId);
        return ResponseEntity.ok(isDuplicate);
    }

    /**
     * 🆕 이메일 중복 확인 API
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailDuplicate(@PathVariable String email) {
        boolean isDuplicate = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok(isDuplicate);
    }
}
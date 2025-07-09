package com.example.demo.service;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.SignupDto;
import com.example.demo.entity.Interest;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.repository.InterestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.util.JwtUtil; // 🔥 JWT 유틸리티 추가
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // 🔥 JWT 유틸리티 주입
    private final UserProfileRepository userProfileRepository;

    /**
     * 회원가입 처리 메서드.
     * 사용자를 생성하고, 기본 프로필을 함께 생성합니다.
     */
    @Transactional
    public void signup(SignupDto dto) {
        // 1. 아이디(userId) 중복 확인
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        // 2. 이메일(email) 중복 확인
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 3. 관심분야 엔티티 조회
        List<Interest> interests = new ArrayList<>();
        if (dto.getInterests() != null && !dto.getInterests().isEmpty()) {
            interests = interestRepository.findByNameIn(dto.getInterests());
        }

        // 4. User 엔티티 생성
        User user = User.builder()
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .isActive(true)
                .interests(interests)
                .build();

        userRepository.save(user);

        // [MODIFIED] 5. 회원가입 시 기본 UserProfile 생성
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        userProfile.setName(user.getName());
        userProfile.setEmail(user.getEmail());
        userProfile.setCareerType("신입"); // 기본값 설정
        userProfile.setJobTitle("미정");   // 기본값 설정
        userProfile.setMatching(true);      // 기본값 설정
        userProfileRepository.save(userProfile);
    }

    /**
     * 로그인 처리 메서드.
     * 인증 성공 시, JWT 토큰과 사용자의 주요 정보를 담은 DTO를 반환합니다.
     */
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginDto dto) {
        // userId로 사용자를 찾습니다.
        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 비밀번호를 비교합니다.
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 🔥 JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getUserId(), user.getId());

        // 인증 성공 시, JWT 토큰과 함께 LoginResponseDto에 정보를 담아 반환합니다.
        return new LoginResponseDto(user.getId(), user.getUserId(), user.getName(), token);
    }
}

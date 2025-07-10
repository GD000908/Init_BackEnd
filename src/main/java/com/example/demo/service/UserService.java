package com.example.demo.service;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.SignupDto;
import com.example.demo.entity.Interest;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.InterestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.util.JwtUtil;
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
    private final JwtUtil jwtUtil;
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

        // 🔥 4. 관리자 계정인지 확인하여 role 설정
        UserRole userRole = UserRole.USER; // 기본값
        if ("admin".equals(dto.getUserId()) || dto.getUserId().startsWith("admin")) {
            userRole = UserRole.ADMIN;
        }

        // 5. User 엔티티 생성
        User user = User.builder()
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .isActive(true)
                .role(userRole) // 🔥 역할 설정
                .interests(interests)
                .build();

        userRepository.save(user);

        // 6. 일반 사용자만 기본 UserProfile 생성 (관리자는 프로필 불필요)
        if (userRole == UserRole.USER) {
            UserProfile userProfile = new UserProfile();
            userProfile.setUser(user);
            userProfile.setName(user.getName());
            userProfile.setEmail(user.getEmail());
            userProfile.setCareerType("신입");
            userProfile.setJobTitle("미정");
            userProfile.setMatching(true);
            userProfileRepository.save(userProfile);
        }
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

        // 계정 활성화 상태 확인
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }

        // 🔥 JWT 토큰 생성 (역할 정보 포함)
        String token = jwtUtil.generateToken(user.getUserId(), user.getId(), user.getRole().name());

        // 🔥 인증 성공 시, JWT 토큰과 역할 정보를 함께 반환
        return new LoginResponseDto(
                user.getId(),
                user.getUserId(),
                user.getName(),
                token,
                user.getRole()
        );
    }

    /**
     * 아이디 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean checkUserIdDuplicate(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }
        return userRepository.existsByUserId(userId);
    }

    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean checkEmailDuplicate(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
        return userRepository.existsByEmail(email);
    }
}
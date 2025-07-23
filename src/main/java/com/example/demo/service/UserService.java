package com.example.demo.service;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.SignupDto;
import com.example.demo.entity.Interest;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.InterestRepository;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserProfileRepository userProfileRepository;
    private final EmailService emailService;

    /**
     * 일반 회원가입 처리 메서드.
     * 사용자를 생성하고, 기본 프로필을 함께 생성합니다.
     */
    @Transactional
    public void signup(SignupDto dto) {
        log.info("👤 회원가입 시작: userId={}, email={}", dto.getUserId(), dto.getEmail());

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

        // 4. 관리자 계정인지 확인하여 role 설정
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
                .role(userRole)
                .interests(interests)
                .build();

        User savedUser = userRepository.save(user);

        // 6. 일반 사용자만 기본 UserProfile 생성 (관리자는 프로필 불필요)
        if (userRole == UserRole.USER) {
            createDefaultUserProfile(savedUser);
        }

        log.info("✅ 회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
    }

    /**
     * 로그인 처리 메서드.
     * 인증 성공 시, JWT 토큰과 사용자의 주요 정보를 담은 DTO를 반환합니다.
     */
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginDto dto) {
        log.info("🔐 로그인 시도: userId={}", dto.getUserId());

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

        // JWT 토큰 생성 (역할 정보 포함)
        String token = jwtUtil.generateToken(user.getUserId(), user.getId(), user.getRole().name());

        log.info("✅ 로그인 성공: userId={}, role={}", user.getUserId(), user.getRole());

        // 인증 성공 시, JWT 토큰과 역할 정보를 함께 반환
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

    /**
     * 이메일로 기존 사용자 조회
     * @param email 이메일 주소
     * @return 사용자 엔티티 (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ===================계정 찾기 관련 메서드들 ===================

    /**
     * 이메일로 아이디 찾기
     * @param email 이메일 주소
     * @return 사용자 아이디
     */
    @Transactional(readOnly = true)
    public String findUserIdByEmail(String email) {
        log.info("🔍 이메일로 아이디 찾기: email={}", email);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new IllegalArgumentException("해당 이메일로 가입된 계정이 없습니다.");
        }

        if (!user.get().getIsActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }

        log.info("✅ 아이디 찾기 성공: email={}, userId={}", email, user.get().getUserId());
        return user.get().getUserId();
    }

    /**
     * 비밀번호 재설정을 위한 인증 코드 발송
     * @param userId 사용자 아이디
     * @param email 이메일 주소
     * @param session HTTP 세션
     * @return 처리 결과
     */
    @Transactional(readOnly = true)
    public String sendPasswordResetCode(String userId, String email, HttpSession session) {
        log.info("🔐 비밀번호 재설정 코드 발송: userId={}, email={}", userId, email);

        // 1. 입력값 검증
        if (userId == null || userId.trim().isEmpty()) {
            return "아이디를 입력해주세요.";
        }
        if (email == null || email.trim().isEmpty()) {
            return "이메일을 입력해주세요.";
        }

        // 2. 사용자 존재 여부 및 이메일 일치 확인
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            return "존재하지 않는 아이디입니다.";
        }

        User user = userOpt.get();

        if (!user.getIsActive()) {
            return "비활성화된 계정입니다. 관리자에게 문의하세요.";
        }

        if (!user.getEmail().equals(email)) {
            return "아이디와 이메일이 일치하지 않습니다.";
        }

        // 3. 인증 코드 생성 및 발송
        try {
            String authCode = generateAuthCode();

            // 이메일 발송
            emailService.sendPasswordResetCode(email, authCode);

            // 세션에 인증 정보 저장 (5분 후 만료)
            session.setAttribute("passwordResetCode", authCode);
            session.setAttribute("passwordResetUserId", userId);
            session.setAttribute("passwordResetEmail", email);
            session.setAttribute("passwordResetTime", System.currentTimeMillis());
            session.setMaxInactiveInterval(300); // 5분

            log.info("✅ 비밀번호 재설정 코드 발송 완료: userId={}", userId);
            return "success";

        } catch (Exception e) {
            log.error("❌ 비밀번호 재설정 코드 발송 실패: userId={}", userId, e);
            return "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    /**
     * 비밀번호 재설정 인증 코드 검증
     * @param userId 사용자 아이디
     * @param email 이메일 주소
     * @param code 인증 코드
     * @param session HTTP 세션
     * @return 검증 결과
     */
    public String verifyPasswordResetCode(String userId, String email, String code, HttpSession session) {
        log.info("🔐 비밀번호 재설정 코드 검증: userId={}, email={}", userId, email);

        try {
            String savedCode = (String) session.getAttribute("passwordResetCode");
            String savedUserId = (String) session.getAttribute("passwordResetUserId");
            String savedEmail = (String) session.getAttribute("passwordResetEmail");
            Long savedTime = (Long) session.getAttribute("passwordResetTime");

            // 1. 세션에 저장된 데이터 확인
            if (savedCode == null || savedUserId == null || savedEmail == null || savedTime == null) {
                return "인증 코드가 만료되었습니다. 다시 요청해주세요.";
            }

            // 2. 5분 만료 확인
            long currentTime = System.currentTimeMillis();
            if (currentTime - savedTime > 300000) { // 5분 = 300,000ms
                // 만료된 세션 데이터 삭제
                clearPasswordResetSession(session);
                return "인증 코드가 만료되었습니다. 다시 요청해주세요.";
            }

            // 3. 사용자 정보 일치 확인
            if (!savedUserId.equals(userId) || !savedEmail.equals(email)) {
                return "인증을 요청한 계정 정보와 일치하지 않습니다.";
            }

            // 4. 인증 코드 일치 확인
            if (!savedCode.equals(code)) {
                return "인증 코드가 올바르지 않습니다.";
            }

            // 5. 인증 성공 - 비밀번호 재설정 권한 부여
            session.setAttribute("passwordResetVerified", true);
            session.setAttribute("passwordResetVerifiedTime", System.currentTimeMillis());

            log.info("✅ 비밀번호 재설정 인증 성공: userId={}", userId);
            return "success";

        } catch (Exception e) {
            log.error("❌ 비밀번호 재설정 인증 검증 실패: userId={}", userId, e);
            return "인증 처리 중 오류가 발생했습니다.";
        }
    }

    /**
     * 비밀번호 재설정 (인증 완료 후)
     * @param userId 사용자 아이디
     * @param email 이메일 주소
     * @param newPassword 새 비밀번호
     * @param session HTTP 세션
     * @return 처리 결과
     */
    @Transactional
    public String resetPassword(String userId, String email, String newPassword, HttpSession session) {
        log.info("🔐 비밀번호 재설정 실행: userId={}, email={}", userId, email);

        try {
            // 1. 인증 완료 여부 확인
            Boolean isVerified = (Boolean) session.getAttribute("passwordResetVerified");
            String savedUserId = (String) session.getAttribute("passwordResetUserId");
            String savedEmail = (String) session.getAttribute("passwordResetEmail");
            Long verifiedTime = (Long) session.getAttribute("passwordResetVerifiedTime");

            if (isVerified == null || !isVerified || savedUserId == null || savedEmail == null || verifiedTime == null) {
                return "인증이 완료되지 않았습니다. 다시 인증해주세요.";
            }

            // 2. 인증 후 10분 이내인지 확인
            long currentTime = System.currentTimeMillis();
            if (currentTime - verifiedTime > 600000) { // 10분 = 600,000ms
                clearPasswordResetSession(session);
                return "인증이 만료되었습니다. 다시 인증해주세요.";
            }

            // 3. 사용자 정보 일치 확인
            if (!savedUserId.equals(userId) || !savedEmail.equals(email)) {
                return "인증된 계정 정보와 일치하지 않습니다.";
            }

            // 4. 새 비밀번호 유효성 검사
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return "새 비밀번호를 입력해주세요.";
            }

            if (newPassword.length() < 8) {
                return "비밀번호는 8자 이상이어야 합니다.";
            }

            if (!newPassword.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).+$")) {
                return "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.";
            }

            // 5. 사용자 조회 및 비밀번호 변경
            Optional<User> userOpt = userRepository.findByUserId(userId);
            if (userOpt.isEmpty()) {
                return "사용자를 찾을 수 없습니다.";
            }

            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // 6. 세션 정리
            clearPasswordResetSession(session);

            log.info("✅ 비밀번호 재설정 완료: userId={}", userId);
            return "success";

        } catch (Exception e) {
            log.error("❌ 비밀번호 재설정 실패: userId={}", userId, e);
            return "비밀번호 재설정 중 오류가 발생했습니다.";
        }
    }

    /**
     * 인증 코드 생성 (6자리 숫자)
     * @return 인증 코드
     */
    private String generateAuthCode() {
        Random random = new Random();
        StringBuilder authCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            authCode.append(random.nextInt(10));
        }
        return authCode.toString();
    }

    /**
     * 비밀번호 재설정 관련 세션 데이터 정리
     * @param session HTTP 세션
     */
    private void clearPasswordResetSession(HttpSession session) {
        session.removeAttribute("passwordResetCode");
        session.removeAttribute("passwordResetUserId");
        session.removeAttribute("passwordResetEmail");
        session.removeAttribute("passwordResetTime");
        session.removeAttribute("passwordResetVerified");
        session.removeAttribute("passwordResetVerifiedTime");
    }

    /**
     * 기본 사용자 프로필 생성
     * @param user 사용자 엔티티
     */
    private void createDefaultUserProfile(User user) {
        try {
            UserProfile userProfile = new UserProfile();
            userProfile.setUser(user);
            userProfile.setName(user.getName());
            userProfile.setEmail(user.getEmail());
            userProfile.setCareerType("신입");
            userProfile.setJobTitle("미정");
            userProfile.setMatching(true);
            userProfileRepository.save(userProfile);

            log.info("✅ 기본 UserProfile 생성 완료: userId={}", user.getUserId());
        } catch (Exception e) {
            log.error("❌ UserProfile 생성 실패: userId={}, error={}", user.getUserId(), e.getMessage());
            // 프로필 생성 실패해도 사용자 생성은 성공으로 처리
        }
    }

    /**
     * 사용자 ID로 사용자 조회 (관리자용)
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 사용자 활성화/비활성화
     * @param userId 사용자 ID
     * @param isActive 활성화 여부
     */
    @Transactional
    public void updateUserActiveStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setIsActive(isActive);
        userRepository.save(user);

        log.info("🔄 사용자 상태 변경: userId={}, isActive={}", userId, isActive);
    }
}
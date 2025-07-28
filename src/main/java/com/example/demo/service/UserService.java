// UserService.java - 비밀번호 재설정 전역 저장소 추가

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    // 🔥 비밀번호 재설정 전역 저장소
    private static final ConcurrentHashMap<String, PasswordResetData> globalPasswordResetStorage = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService passwordResetCleanupService = Executors.newScheduledThreadPool(1);

    // 🔥 정리 작업 스케줄러 (1분마다 만료된 데이터 삭제)
    static {
        passwordResetCleanupService.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            globalPasswordResetStorage.entrySet().removeIf(entry -> {
                boolean isExpired = currentTime - entry.getValue().getTimestamp() > 1800000; // 30분

                return isExpired;
            });
        }, 1, 1, TimeUnit.MINUTES);
    }

    // 🔥 비밀번호 재설정 데이터 저장 클래스
    private static class PasswordResetData {
        private final String userId;
        private final String email;
        private final String code;
        private final String sessionId;
        private final long timestamp;
        private boolean verified;

        public PasswordResetData(String userId, String email, String code, String sessionId, long timestamp) {
            this.userId = userId;
            this.email = email;
            this.code = code;
            this.sessionId = sessionId;
            this.timestamp = timestamp;
            this.verified = false;
        }

        // Getters and setters
        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getCode() { return code; }
        public String getSessionId() { return sessionId; }
        public long getTimestamp() { return timestamp; }
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
    }

    // 기존 회원가입, 로그인 등 메서드들은 그대로 유지...

    /**
     * 일반 회원가입 처리 메서드.
     */
    @Transactional
    public void signup(SignupDto dto) {
        log.info("👤 회원가입 시작: userId={}, email={}", dto.getUserId(), dto.getEmail());

        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        List<Interest> interests = new ArrayList<>();
        if (dto.getInterests() != null && !dto.getInterests().isEmpty()) {
            interests = interestRepository.findByNameIn(dto.getInterests());
        }

        UserRole userRole = UserRole.USER;
        if ("admin".equals(dto.getUserId()) || dto.getUserId().startsWith("admin")) {
            userRole = UserRole.ADMIN;
        }

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

        if (userRole == UserRole.USER) {
            createDefaultUserProfile(savedUser);
        }

        log.info("✅ 회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
    }

    /**
     * 로그인 처리 메서드.
     */
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginDto dto) {
        log.info("🔐 로그인 시도: userId={}", dto.getUserId());

        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getId(), user.getRole().name());

        log.info("✅ 로그인 성공: userId={}, role={}", user.getUserId(), user.getRole());

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
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ===================계정 찾기 관련 메서드들 ===================

    /**
     * 이메일로 아이디 찾기
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
     * 🔥 비밀번호 재설정을 위한 인증 코드 발송 (전역 저장소 사용)
     */
    @Transactional(readOnly = true)
    public String sendPasswordResetCode(String userId, String email, HttpSession session) {
        log.info("🔐 [PWD_RESET_SEND] 비밀번호 재설정 코드 발송: userId={}, email={}", userId, email);

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
            String sessionId = session.getId();
            long currentTime = System.currentTimeMillis();

            // 이메일 발송
            emailService.sendPasswordResetCode(email, authCode);

            // 4. 🔥 기존 세션 저장 (기본)
            session.setAttribute("passwordResetCode", authCode);
            session.setAttribute("passwordResetUserId", userId);
            session.setAttribute("passwordResetEmail", email);
            session.setAttribute("passwordResetTime", currentTime);
            session.setMaxInactiveInterval(1800); // 30분

            // 5. 🔥 전역 저장소에도 저장 (모바일 백업용)
            String storageKey = userId + ":" + email; // 복합 키 사용
            PasswordResetData resetData = new PasswordResetData(userId, email, authCode, sessionId, currentTime);
            globalPasswordResetStorage.put(storageKey, resetData);

            log.info("🔥 [PWD_GLOBAL] 전역 저장소에 비밀번호 재설정 데이터 저장: key={}, sessionId={}",
                    storageKey, sessionId.substring(0, 8));

            log.info("✅ [PWD_RESET_SEND] 비밀번호 재설정 코드 발송 완료: userId={}", userId);
            return "success";

        } catch (Exception e) {
            log.error("❌ [PWD_RESET_SEND] 비밀번호 재설정 코드 발송 실패: userId={}", userId, e);
            return "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    /**
     * 🔥 전역 저장소에서 비밀번호 재설정 코드 검증
     */
    public String verifyPasswordResetCodeGlobal(String userId, String email, String code) {
        try {
            String storageKey = userId + ":" + email;
            log.info("🔐 [PWD_GLOBAL] 전역 저장소에서 비밀번호 재설정 코드 검증: key={}", storageKey);

            PasswordResetData resetData = globalPasswordResetStorage.get(storageKey);

            if (resetData == null) {
                log.warn("❌ [PWD_GLOBAL] 전역 저장소에 데이터 없음: key={}", storageKey);
                return "인증 코드가 만료되었습니다. 다시 요청해주세요.";
            }

            // 1. 만료 시간 확인 (30분)
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - resetData.getTimestamp();

            if (elapsedTime > 1800000) { // 30분
                globalPasswordResetStorage.remove(storageKey);
                log.warn("❌ [PWD_GLOBAL] 인증 코드 만료: key={}, elapsed={}ms", storageKey, elapsedTime);
                return "인증 코드가 만료되었습니다. 다시 요청해주세요.";
            }

            // 2. 데이터 일치 확인
            if (!resetData.getUserId().equals(userId) || !resetData.getEmail().equals(email)) {
                log.warn("❌ [PWD_GLOBAL] 사용자 정보 불일치: key={}", storageKey);
                return "인증을 요청한 계정 정보와 일치하지 않습니다.";
            }

            // 3. 인증 코드 확인
            if (!resetData.getCode().equals(code)) {
                log.warn("❌ [PWD_GLOBAL] 인증 코드 불일치: key={}", storageKey);
                return "인증 코드가 올바르지 않습니다.";
            }

            // 4. 인증 성공
            resetData.setVerified(true);
            log.info("✅ [PWD_GLOBAL] 전역 저장소 비밀번호 재설정 인증 성공: key={}, sessionId={}",
                    storageKey, resetData.getSessionId().substring(0, 8));

            return "success";

        } catch (Exception e) {
            log.error("❌ [PWD_GLOBAL] 전역 저장소 비밀번호 재설정 인증 검증 실패: userId={}, email={}", userId, email, e);
            return "인증 처리 중 오류가 발생했습니다.";
        }
    }

    /**
     * 🔥 비밀번호 재설정 인증 코드 검증 (개선된 버전)
     */
    public String verifyPasswordResetCode(String userId, String email, String code, HttpSession session) {
        log.info("🔐 [PWD_RESET_VERIFY] 비밀번호 재설정 코드 검증: userId={}, email={}, sessionId={}",
                userId, email, session.getId());

        try {
            String savedCode = (String) session.getAttribute("passwordResetCode");
            String savedUserId = (String) session.getAttribute("passwordResetUserId");
            String savedEmail = (String) session.getAttribute("passwordResetEmail");
            Long savedTime = (Long) session.getAttribute("passwordResetTime");

            // 🔥 디버깅 로그
            log.info("🔍 [PWD_VERIFY] 세션 데이터 확인:");
            log.info("  - savedCode: {}", savedCode != null ? "EXISTS" : "NULL");
            log.info("  - savedUserId: {}", savedUserId);
            log.info("  - savedEmail: {}", savedEmail);
            log.info("  - savedTime: {}", savedTime);

            // 🔥 세션 데이터가 없다면 전역 저장소에서 시도
            if (savedCode == null || savedUserId == null || savedEmail == null || savedTime == null) {
                log.warn("⚠️ [PWD_VERIFY] 세션 데이터 없음, 전역 저장소에서 검증 시도");
                String globalResult = verifyPasswordResetCodeGlobal(userId, email, code);

                if ("success".equals(globalResult)) {
                    // 🔥 전역 저장소 성공시 세션에도 인증 완료 표시
                    session.setAttribute("passwordResetVerified", true);
                    session.setAttribute("passwordResetVerifiedTime", System.currentTimeMillis());
                    session.setAttribute("passwordResetUserId", userId);
                    session.setAttribute("passwordResetEmail", email);
                }

                return globalResult;
            }

            // 기존 세션 기반 검증 로직
            long currentTime = System.currentTimeMillis();
            if (currentTime - savedTime > 1800000) { // 30분
                clearPasswordResetSession(session);
                log.warn("❌ [PWD_VERIFY] 세션 인증 코드 만료, 전역 저장소에서 재시도");
                return verifyPasswordResetCodeGlobal(userId, email, code);
            }

            if (!savedUserId.equals(userId) || !savedEmail.equals(email)) {
                return "인증을 요청한 계정 정보와 일치하지 않습니다.";
            }

            if (!savedCode.equals(code)) {
                return "인증 코드가 올바르지 않습니다.";
            }

            // 인증 성공
            session.setAttribute("passwordResetVerified", true);
            session.setAttribute("passwordResetVerifiedTime", currentTime);

            // 🔥 전역 저장소도 업데이트
            String storageKey = userId + ":" + email;
            PasswordResetData resetData = globalPasswordResetStorage.get(storageKey);
            if (resetData != null) {
                resetData.setVerified(true);
            }

            log.info("✅ [PWD_VERIFY] 비밀번호 재설정 인증 성공: userId={}", userId);
            return "success";

        } catch (Exception e) {
            log.error("❌ [PWD_VERIFY] 비밀번호 재설정 인증 검증 실패: userId={}", userId, e);
            return "인증 처리 중 오류가 발생했습니다.";
        }
    }

    /**
     * 🔥 비밀번호 재설정 (개선된 버전)
     */
    @Transactional
    public String resetPassword(String userId, String email, String newPassword, HttpSession session) {
        log.info("🔐 [PWD_RESET] 비밀번호 재설정 실행: userId={}, email={}", userId, email);

        try {
            // 1. 세션 기반 인증 확인
            Boolean isVerified = (Boolean) session.getAttribute("passwordResetVerified");
            String savedUserId = (String) session.getAttribute("passwordResetUserId");
            String savedEmail = (String) session.getAttribute("passwordResetEmail");
            Long verifiedTime = (Long) session.getAttribute("passwordResetVerifiedTime");

            boolean sessionVerified = false;

            if (isVerified != null && isVerified && savedUserId != null && savedEmail != null && verifiedTime != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - verifiedTime <= 1800000) { // 30분 이내
                    sessionVerified = savedUserId.equals(userId) && savedEmail.equals(email);
                }
            }

            // 2. 🔥 세션 인증 실패 시 전역 저장소에서 확인
            boolean globalVerified = false;
            if (!sessionVerified) {
                log.warn("⚠️ [PWD_RESET] 세션 인증 실패, 전역 저장소에서 확인");
                String storageKey = userId + ":" + email;
                PasswordResetData resetData = globalPasswordResetStorage.get(storageKey);

                if (resetData != null && resetData.isVerified()) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - resetData.getTimestamp() <= 1800000) { // 30분 이내
                        globalVerified = true;
                        log.info("✅ [PWD_RESET] 전역 저장소 인증 확인 성공");
                    }
                }
            }

            if (!sessionVerified && !globalVerified) {
                return "인증이 완료되지 않았습니다. 다시 인증해주세요.";
            }

            // 3. 새 비밀번호 유효성 검사
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return "새 비밀번호를 입력해주세요.";
            }

            if (newPassword.length() < 8) {
                return "비밀번호는 8자 이상이어야 합니다.";
            }

            if (!newPassword.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).+$")) {
                return "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.";
            }

            // 4. 사용자 조회 및 비밀번호 변경
            Optional<User> userOpt = userRepository.findByUserId(userId);
            if (userOpt.isEmpty()) {
                return "사용자를 찾을 수 없습니다.";
            }

            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // 5. 세션 및 전역 저장소 정리
            clearPasswordResetSession(session);
            String storageKey = userId + ":" + email;
            globalPasswordResetStorage.remove(storageKey);

            log.info("✅ [PWD_RESET] 비밀번호 재설정 완료: userId={}", userId);
            return "success";

        } catch (Exception e) {
            log.error("❌ [PWD_RESET] 비밀번호 재설정 실패: userId={}", userId, e);
            return "비밀번호 재설정 중 오류가 발생했습니다.";
        }
    }

    /**
     * 🔥 전역 저장소 상태 확인 (디버깅용)
     */
    public void logPasswordResetStorageStatus() {
        log.info("📊 [PWD_GLOBAL] 비밀번호 재설정 전역 저장소 상태: 총 {}개 항목", globalPasswordResetStorage.size());
        globalPasswordResetStorage.forEach((key, resetData) -> {
            log.info("  - {}: sessionId={}, verified={}, age={}분",
                    key,
                    resetData.getSessionId().substring(0, 8),
                    resetData.isVerified(),
                    (System.currentTimeMillis() - resetData.getTimestamp()) / 60000);
        });
    }

    /**
     * 인증 코드 생성 (6자리 숫자)
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
        }
    }

    /**
     * 사용자 ID로 사용자 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 사용자 활성화/비활성화
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
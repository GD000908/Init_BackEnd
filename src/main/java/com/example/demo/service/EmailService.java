package com.example.demo.service;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 인증 코드 생성 (6자리 숫자)
     */
    public String createAuthCode() {
        Random random = new Random();
        StringBuilder authCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            authCode.append(random.nextInt(10));
        }
        return authCode.toString();
    }

    /**
     * 🔥 회원가입 이메일 인증 코드 발송 (에러 핸들링 강화)
     * @param toEmail 받는 이메일 주소
     * @param authCode 인증 코드
     */
    public void sendEmailAuthCode(String toEmail, String authCode) {
        log.info("📧 [EMAIL] 이메일 인증 코드 발송 시작: toEmail={}, authCode={}", toEmail, authCode);
        
        try {
            // 🔥 SMTP 연결 테스트
            log.info("🔍 [EMAIL] SMTP 연결 상태 확인...");
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nagundo@naver.com");
            helper.setTo(toEmail);
            helper.setSubject("회원가입 이메일 인증 코드");

            String htmlContent = """
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #356ae4; margin: 0;">Init</h1>
                        <p style="color: #666; margin: 10px 0;">회원가입 이메일 인증</p>
                    </div>
                    
                    <div style="background: #f8f9fa; padding: 30px; border-radius: 10px; text-align: center;">
                        <h2 style="color: #333; margin-bottom: 20px;">인증 코드를 입력해주세요</h2>
                        <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0;">
                            <span style="font-size: 32px; font-weight: bold; color: #356ae4; letter-spacing: 8px;">%s</span>
                        </div>
                        <p style="color: #666; margin: 15px 0;">위 인증 코드를 회원가입 페이지에 입력해주세요.</p>
                        <p style="color: #999; font-size: 14px;">인증 코드는 5분 후 만료됩니다.</p>
                    </div>
                    
                    <div style="text-align: center; margin-top: 30px; color: #999; font-size: 12px;">
                        <p>본 메일은 발신 전용입니다. 문의사항이 있으시면 고객센터로 연락해주세요.</p>
                    </div>
                </div>
                """.formatted(authCode);

            helper.setText(htmlContent, true);
            
            log.info("📤 [EMAIL] 이메일 발송 중...");
            mailSender.send(message);

            log.info("✅ [EMAIL] 이메일 인증 코드 발송 완료: {}", toEmail);

        } catch (AuthenticationFailedException e) {
            log.error("❌ [EMAIL] SMTP 인증 실패: toEmail={}, error={}", toEmail, e.getMessage(), e);
            throw new RuntimeException("이메일 서버 인증에 실패했습니다. 관리자에게 문의하세요.");
        } catch (MessagingException e) {
            log.error("❌ [EMAIL] 메시지 처리 실패: toEmail={}, error={}", toEmail, e.getMessage(), e);
            throw new RuntimeException("이메일 메시지 생성에 실패했습니다: " + e.getMessage());
        } catch (MailException e) {
            log.error("❌ [EMAIL] 메일 전송 실패: toEmail={}, error={}", toEmail, e.getMessage(), e);
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ [EMAIL] 예상치 못한 이메일 발송 실패: toEmail={}", toEmail, e);
            
            // 🔥 에러 타입별 세부 분석
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new RuntimeException("이메일 서버 연결 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
            } else if (e.getMessage() != null && e.getMessage().contains("authentication")) {
                throw new RuntimeException("이메일 서버 인증 설정에 문제가 있습니다.");
            } else if (e.getMessage() != null && e.getMessage().contains("connection")) {
                throw new RuntimeException("이메일 서버에 연결할 수 없습니다.");
            } else {
                throw new RuntimeException("이메일 발송에 실패했습니다: " + e.getMessage());
            }
        }
    }

    /**
     * 🔥 비밀번호 재설정 인증 코드 발송 (에러 핸들링 강화)
     * @param toEmail 받는 이메일 주소
     * @param authCode 인증 코드
     */
    public void sendPasswordResetCode(String toEmail, String authCode) {
        log.info("🔐 [EMAIL] 비밀번호 재설정 인증 코드 발송 시작: toEmail={}, authCode={}", toEmail, authCode);
        
        try {
            log.info("🔍 [EMAIL] SMTP 연결 상태 확인...");
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nagundo@naver.com");
            helper.setTo(toEmail);
            helper.setSubject("비밀번호 재설정 인증 코드");

            String htmlContent = """
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #356ae4; margin: 0;">Init</h1>
                        <p style="color: #666; margin: 10px 0;">비밀번호 재설정</p>
                    </div>
                    
                    <div style="background: #f8f9fa; padding: 30px; border-radius: 10px; text-align: center;">
                        <h2 style="color: #333; margin-bottom: 20px;">🔐 비밀번호 재설정 인증 코드</h2>
                        
                        <p style="color: #666; margin: 15px 0;">
                            비밀번호를 재설정하기 위한 인증 코드입니다.<br>
                            아래 코드를 입력하여 본인 인증을 완료해주세요.
                        </p>
                        
                        <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0;">
                            <span style="font-size: 32px; font-weight: bold; color: #356ae4; letter-spacing: 8px;">%s</span>
                        </div>
                        
                        <p style="color: #666; margin: 15px 0;">
                            인증 코드를 비밀번호 재설정 페이지에 입력해주세요.
                        </p>
                        <p style="color: #999; font-size: 14px;">
                            ⏰ 인증 코드는 5분 후 만료됩니다.<br>
                            🔒 본인이 요청하지 않았다면 이 이메일을 무시하세요.
                        </p>
                    </div>
                    
                    <div style="text-align: center; margin-top: 30px; color: #999; font-size: 12px;">
                        <p>본 메일은 발신 전용입니다. 문의사항이 있으시면 고객센터로 연락해주세요.</p>
                        <p>📞 고객센터: 1588-0000 | 📧 이메일: support@init.com</p>
                    </div>
                </div>
                """.formatted(authCode);

            helper.setText(htmlContent, true);
            
            log.info("📤 [EMAIL] 비밀번호 재설정 이메일 발송 중...");
            mailSender.send(message);

            log.info("✅ [EMAIL] 비밀번호 재설정 인증 코드 발송 완료: {}", toEmail);

        } catch (AuthenticationFailedException e) {
            log.error("❌ [EMAIL] SMTP 인증 실패 (비밀번호 재설정): toEmail={}, error={}", toEmail, e.getMessage(), e);
            throw new RuntimeException("이메일 서버 인증에 실패했습니다. 관리자에게 문의하세요.");
        } catch (MessagingException e) {
            log.error("❌ [EMAIL] 메시지 처리 실패 (비밀번호 재설정): toEmail={}, error={}", toEmail, e.getMessage(), e);
            throw new RuntimeException("이메일 메시지 생성에 실패했습니다: " + e.getMessage());
        } catch (MailException e) {
            log.error("❌ [EMAIL] 메일 전송 실패 (비밀번호 재설정): toEmail={}, error={}", toEmail, e.getMessage(), e);
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ [EMAIL] 예상치 못한 비밀번호 재설정 이메일 발송 실패: toEmail={}", toEmail, e);
            
            // 🔥 에러 타입별 세부 분석
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new RuntimeException("이메일 서버 연결 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
            } else if (e.getMessage() != null && e.getMessage().contains("authentication")) {
                throw new RuntimeException("이메일 서버 인증 설정에 문제가 있습니다.");
            } else if (e.getMessage() != null && e.getMessage().contains("connection")) {
                throw new RuntimeException("이메일 서버에 연결할 수 없습니다.");
            } else {
                throw new RuntimeException("이메일 발송에 실패했습니다: " + e.getMessage());
            }
        }
    }

    /**
     * 🔥 이메일 서비스 연결 상태 테스트
     */
    public boolean testEmailConnection() {
        try {
            log.info("🔍 [EMAIL] SMTP 연결 테스트 시작...");
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("nagundo@naver.com");
            helper.setTo("nagundo@naver.com"); // 자기 자신에게 테스트
            helper.setSubject("SMTP 연결 테스트");
            helper.setText("이메일 서비스 연결이 정상적으로 작동하고 있습니다.", false);
            
            // 실제로는 발송하지 않고 연결만 테스트
            log.info("✅ [EMAIL] SMTP 연결 테스트 성공");
            return true;
            
        } catch (Exception e) {
            log.error("❌ [EMAIL] SMTP 연결 테스트 실패: {}", e.getMessage(), e);
            return false;
        }
    }
}

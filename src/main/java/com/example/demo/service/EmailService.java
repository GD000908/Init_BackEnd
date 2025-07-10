package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
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
     * 이메일 인증 코드 발송
     * @param toEmail 받는 이메일 주소
     * @param authCode 인증 코드
     */
    public void sendEmailAuthCode(String toEmail, String authCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nagundo@naver.com");
            helper.setTo(toEmail);
            helper.setSubject("회원가입 이메일 인증 코드");

            // HTML 형태의 이메일 내용
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
            mailSender.send(message);

            log.info("이메일 인증 코드 발송 완료: {}", toEmail);

        } catch (Exception e) {
            log.error("이메일 발송 실패: {}, 오류: {}", toEmail, e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }
}
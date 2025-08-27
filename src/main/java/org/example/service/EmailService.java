package org.example.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    public void sendVerificationCode(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("olion7234@gmail.com", "CancerCare");  // 발신자명 설정
            helper.setTo(email);
            helper.setSubject("✨ 회원가입 인증");
            
            String htmlContent = createEmailTemplate(code, "회원가입");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("이메일 발송 성공: " + email + " -> " + code);
        } catch (Exception e) {
            System.out.println("이메일 발송 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    public boolean sendPasswordResetCode(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("olion7234@gmail.com", "CancerCare");  // 발신자명 설정
            helper.setTo(email);
            helper.setSubject("🔒 비밀번호 재설정 인증");
            
            String htmlContent = createEmailTemplate(code, "비밀번호 재설정");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("비밀번호 재설정 이메일 발송 성공: " + email + " -> " + code);
            return true;
        } catch (Exception e) {
            System.out.println("비밀번호 재설정 이메일 발송 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private String createEmailTemplate(String code, String type) {
        if ("비밀번호 재설정".equals(type)) {
            return createPasswordResetTemplate(code);
        } else {
            return createSignupTemplate(code);
        }
    }
    
    private String createSignupTemplate(String code) {
        String html = """
            <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; font-family: Arial, sans-serif;">
                
                <!-- 헤더 -->
                <div style="background-color: #ffffff; padding: 30px; text-align: center; border-bottom: 1px solid #f0f0f0;">
                    <h1 style="color: #4ECDC4; margin: 0; font-size: 24px; font-weight: 600;">✨ 회원가입 인증</h1>
                </div>
                
                <!-- 콘텐츠 -->
                <div style="padding: 30px; text-align: center; background-color: #ffffff;">
                    <div style="font-size: 18px; color: #333333; margin-bottom: 20px; font-weight: 600;">이메일 인증번호를 확인해주세요</div>
                    <div style="font-size: 14px; color: #666666; margin-bottom: 30px; line-height: 1.5;">
                        안녕하세요!<br>
                        회원가입을 위한 인증번호입니다.
                    </div>
                    
                    <!-- 인증번호 박스 -->
                    <div style="background-color: #4ECDC4; color: white; font-size: 32px; font-weight: bold; padding: 20px; text-align: center; margin: 30px 0; letter-spacing: 6px;">
                        ${CODE}
                    </div>
                    
                    <!-- 안내사항 -->
                    <div style="background-color: #f8fcfc; border-left: 4px solid #4ECDC4; padding: 20px; margin: 30px 0; text-align: left;">
                        <div style="margin: 0 0 15px 0; color: #333333; font-size: 16px; font-weight: bold;">📋 인증 안내</div>
                        <div style="color: #555555; font-size: 14px; line-height: 1.6;">
                            • 위의 6자리 인증번호를 회원가입 페이지에 입력해주세요<br>
                            • 인증번호는 <strong style="color: #4ECDC4;">5분간</strong> 유효합니다<br>
                            • 인증번호 입력은 최대 5번까지 가능합니다<br>
                            • 시간이 초과되거나 횟수를 초과한 경우 새로운 인증번호를 요청해주세요
                        </div>
                    </div>
                    
                    <!-- 보안 경고 -->
                    <div style="background-color: #fff8e1; border: 1px solid #ffa726; padding: 15px; margin: 20px 0; text-align: left;">
                        <div style="color: #e65100; font-size: 13px; line-height: 1.4;">
                            <strong>⚠️ 보안 안내</strong><br>
                            본인이 요청하지 않은 이메일일 경우 무시하시기 바랍니다. 다른 사람에게 인증번호를 알려주지 마세요.
                        </div>
                    </div>
                </div>
                
                <!-- 푸터 -->
                <div style="background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #666666;">
                    © 2025 <span style="color: #4ECDC4; font-weight: 600;">CancerCare</span>. 모든 권리 보유.
                </div>
            </div>
            """;
        return html.replace("${CODE}", code);
    }

    private String createPasswordResetTemplate(String code) {
        String html = """
            <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; font-family: Arial, sans-serif;">
                
                <!-- 헤더 -->
                <div style="background-color: #ffffff; padding: 30px; text-align: center; border-bottom: 1px solid #f0f0f0;">
                    <h1 style="color: #4ECDC4; margin: 0; font-size: 24px; font-weight: 600;">🔒 비밀번호 재설정</h1>
                </div>
                
                <!-- 콘텐츠 -->
                <div style="padding: 30px; text-align: center; background-color: #ffffff;">
                    <div style="font-size: 18px; color: #333333; margin-bottom: 20px; font-weight: 600;">비밀번호 재설정 인증번호</div>
                    <div style="font-size: 14px; color: #666666; margin-bottom: 30px; line-height: 1.5;">
                        안녕하세요!<br>
                        비밀번호 재설정을 위한 인증번호입니다.
                    </div>
                    
                    <!-- 인증번호 박스 -->
                    <div style="background-color: #4ECDC4; color: white; font-size: 32px; font-weight: bold; padding: 20px; text-align: center; margin: 30px 0; letter-spacing: 6px;">
                        ${CODE}
                    </div>
                    
                    <!-- 안내사항 -->
                    <div style="background-color: #f8fcfc; border-left: 4px solid #4ECDC4; padding: 20px; margin: 30px 0; text-align: left;">
                        <div style="margin: 0 0 15px 0; color: #333333; font-size: 16px; font-weight: bold;">📋 인증 안내</div>
                        <div style="color: #555555; font-size: 14px; line-height: 1.6;">
                            • 위의 6자리 인증번호를 비밀번호 재설정 페이지에 입력해주세요<br>
                            • 인증번호는 <strong style="color: #4ECDC4;">5분간</strong> 유효합니다<br>
                            • 인증번호 입력은 최대 5번까지 가능합니다<br>
                            • 시간이 초과되거나 횟수를 초과한 경우 새로운 인증번호를 요청해주세요
                        </div>
                    </div>
                    
                    <!-- 보안 경고 -->
                    <div style="background-color: #fff8e1; border: 1px solid #ffa726; padding: 15px; margin: 20px 0; text-align: left;">
                        <div style="color: #e65100; font-size: 13px; line-height: 1.4;">
                            <strong>⚠️ 보안 안내</strong><br>
                            본인이 요청하지 않은 이메일일 경우 무시하시기 바랍니다. 다른 사람에게 인증번호를 알려주지 마세요.
                        </div>
                    </div>
                </div>
                
                <!-- 푸터 -->
                <div style="background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #666666;">
                    © 2025 <span style="color: #4ECDC4; font-weight: 600;">CancerCare</span>. 모든 권리 보유.
                </div>
            </div>
            """;
        return html.replace("${CODE}", code);
    }
}

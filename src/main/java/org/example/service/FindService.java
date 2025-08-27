package org.example.service;

import org.example.domain.EmailVerificationCode;
import org.example.domain.User;
import org.example.repository.EmailVerificationCodeRepository;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class FindService {
    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository codeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public FindService(UserRepository userRepository, EmailVerificationCodeRepository codeRepository, 
                      EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.codeRepository = codeRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 이메일로 로그인 아이디 찾기
     */
    public String findLoginIdByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            String loginId = userOpt.get().getLoginId();
            return loginId; // 마스킹 없이 실제 아이디 반환
        }
        return null;
    }

    /**
     * 비밀번호 재설정 인증코드 발송
     */
    public boolean sendPasswordResetCode(String email) {
        // 가입된 이메일인지 확인
        if (!userRepository.existsByEmail(email)) {
            return false;
        }

        // 기존 인증코드 삭제
        codeRepository.deleteByEmail(email);

        // 새 인증코드 생성
        String code = generateCode();
        EmailVerificationCode verificationCode = new EmailVerificationCode(email, code);
        codeRepository.save(verificationCode);

        // 이메일 발송
        return emailService.sendPasswordResetCode(email, code);
    }

    /**
     * 비밀번호 재설정 인증코드 검증
     */
    public boolean verifyPasswordResetCode(String email, String code) {
        Optional<EmailVerificationCode> codeOpt = codeRepository.findByEmailAndCode(email, code);
        if (codeOpt.isEmpty()) {
            return false;
        }

        EmailVerificationCode verificationCode = codeOpt.get();
        
        // 만료 시간 확인 (5분)
        if (verificationCode.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            codeRepository.delete(verificationCode);
            return false;
        }

        // 시도 횟수 확인 (5회)
        if (verificationCode.getAttemptCount() >= 5) {
            codeRepository.delete(verificationCode);
            return false;
        }

        // 시도 횟수 증가
        verificationCode.setAttemptCount(verificationCode.getAttemptCount() + 1);
        codeRepository.save(verificationCode);

        return true;
    }

    /**
     * 비밀번호 재설정
     */
    public boolean resetPassword(String email, String code, String newPassword) {
        // 인증코드 재검증
        if (!verifyPasswordResetCode(email, code)) {
            return false;
        }

        // 사용자 찾기
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        // 비밀번호 변경
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 인증코드 삭제 (재사용 방지)
        codeRepository.deleteByEmail(email);

        return true;
    }

    /**
     * 6자리 랜덤 숫자 코드 생성
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }
}

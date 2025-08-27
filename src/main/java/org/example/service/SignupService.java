package org.example.service;

import org.example.domain.EmailVerificationCode;
import org.example.domain.User;
import org.example.form.SignupForm;
import org.example.repository.EmailVerificationCodeRepository;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class SignupService {
    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository codeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public SignupService(UserRepository userRepository, EmailVerificationCodeRepository codeRepository, 
                        EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.codeRepository = codeRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean sendVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) return false;
        
        String code = emailService.generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        
        codeRepository.save(new EmailVerificationCode(email, code, expiresAt));
        emailService.sendVerificationCode(email, code);
        return true;
    }

    public boolean verifyCode(String email, String code) {
        return codeRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .map(saved -> !saved.isExpired() && saved.getCode().equals(code))
                .orElse(false);
    }

    public boolean signup(SignupForm form) {
        if (userRepository.existsByLoginId(form.loginId()) || userRepository.existsByEmail(form.email())) {
            return false;
        }
        if (!form.password().equals(form.passwordConfirm())) return false;
        if (!verifyCode(form.email(), form.verificationCode())) return false;

        User user = new User(form.loginId(), form.name(), form.email(), 
                           passwordEncoder.encode(form.password()), form.getGenderEnum(), form.birthdate());
        userRepository.save(user);
        return true;
    }
}

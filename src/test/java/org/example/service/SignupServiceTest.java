package org.example.service;

import org.example.domain.EmailVerificationCode;
import org.example.domain.User;
import org.example.form.SignupForm;
import org.example.repository.EmailVerificationCodeRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationCodeRepository codeRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;

    @Test
    void sendVerificationCode_returnsFalse_whenEmailExists() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);
        assertFalse(signupService.sendVerificationCode("a@b.com"));
        verify(codeRepository, never()).save(any());
    }

    @Test
    void verifyCode_checksLatest_andMatches() {
        EmailVerificationCode latest = new EmailVerificationCode("a@b.com", "123456");
        when(codeRepository.findTopByEmailOrderByCreatedAtDesc("a@b.com")).thenReturn(Optional.of(latest));
        assertTrue(signupService.verifyCode("a@b.com", "123456"));
    }

    @Test
    void signup_success() {
        SignupForm form = new SignupForm("login","name","a@b.com","pw","pw","N", LocalDate.now(), "123456");
        when(userRepository.existsByLoginId("login")).thenReturn(false);
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(codeRepository.findTopByEmailOrderByCreatedAtDesc("a@b.com")).thenReturn(Optional.of(new EmailVerificationCode("a@b.com","123456")));
        when(passwordEncoder.encode("pw")).thenReturn("ENC");

        boolean ok = signupService.signup(form);
        assertTrue(ok);
        verify(userRepository, times(1)).save(any(User.class));
    }
}


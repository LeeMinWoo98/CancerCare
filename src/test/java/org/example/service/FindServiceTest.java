package org.example.service;

import org.example.domain.EmailVerificationCode;
import org.example.domain.User;
import org.example.repository.EmailVerificationCodeRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationCodeRepository codeRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private FindService findService;

    @Test
    void findLoginIdByEmail_returnsLoginId_whenUserExists() {
        User user = new User();
        user.setLoginId("tester");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        String result = findService.findLoginIdByEmail("a@b.com");
        assertEquals("tester", result);
    }

    @Test
    void sendPasswordResetCode_succeeds_forExistingEmail() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);
        when(emailService.sendPasswordResetCode(eq("a@b.com"), anyString())).thenReturn(true);

        boolean ok = findService.sendPasswordResetCode("a@b.com");
        assertTrue(ok);
        verify(codeRepository, times(1)).deleteByEmail("a@b.com");
        verify(codeRepository, times(1)).save(any(EmailVerificationCode.class));
    }

    @Test
    void verifyPasswordResetCode_fails_whenExpired() {
        EmailVerificationCode code = new EmailVerificationCode("a@b.com", "123456", LocalDateTime.now().plusMinutes(5));
        // 서비스 로직은 createdAt+5분을 만료 기준으로 사용하므로 createdAt을 과거로 설정
        code.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        when(codeRepository.findByEmailAndCode("a@b.com", "123456")).thenReturn(Optional.of(code));

        boolean ok = findService.verifyPasswordResetCode("a@b.com", "123456");
        assertFalse(ok);
        verify(codeRepository, times(1)).delete(code);
    }

    @Test
    void resetPassword_success() {
        EmailVerificationCode code = new EmailVerificationCode("a@b.com", "123456", LocalDateTime.now().plusMinutes(5));
        when(codeRepository.findByEmailAndCode("a@b.com", "123456")).thenReturn(Optional.of(code));

        User user = new User("login", "name", "a@b.com", "pw", User.Gender.N, LocalDate.now());
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpw")).thenReturn("ENC");

        boolean ok = findService.resetPassword("a@b.com", "123456", "newpw");
        assertTrue(ok);
        assertEquals("ENC", user.getPassword());
        verify(userRepository, times(1)).save(user);
        verify(codeRepository, times(1)).deleteByEmail("a@b.com");
    }
}


package org.example.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void generateCode() {
        String code = emailService.generateCode();
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));

        // 랜덤성 간단 체크: 여러 번 호출 시 동일 확률 낮음
        String code2 = emailService.generateCode();
        String code3 = emailService.generateCode();
        assertTrue(!(code.equals(code2) && code2.equals(code3)));
    }

    @Test
    void sendVerificationCode() {
        // Given
        JavaMailSenderImpl real = new JavaMailSenderImpl();
        MimeMessage mimeMessage = real.createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendVerificationCode("user@example.com", "123456");

        // Then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendPasswordResetCode() {
        // Success case
        JavaMailSenderImpl real = new JavaMailSenderImpl();
        MimeMessage mimeMessage = real.createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean ok = emailService.sendPasswordResetCode("user@example.com", "654321");
        assertTrue(ok);
        verify(mailSender, times(1)).send(mimeMessage);

        // Failure case: send throws
        reset(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("send fail")).when(mailSender).send(any(MimeMessage.class));
        boolean fail = emailService.sendPasswordResetCode("user@example.com", "111111");
        assertFalse(fail);
    }
}
package org.example.repository;

import org.example.domain.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findTopByEmailOrderByCreatedAtDesc(String email);
    void deleteByExpiresAtBefore(LocalDateTime now);
}

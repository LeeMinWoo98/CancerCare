package org.example.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(name = "login_id", nullable = false, length = 30)
    private String loginId;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false)
    private SenderType sender;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum SenderType {user, chatbot}

    // 기본 생성자
    public ChatMessage() {
    }

    // 🎯 생성자 수정 (loginId 추가)
    public ChatMessage(Diagnosis diagnosis, String loginId, String messageText, SenderType sender) {
        this.diagnosis = diagnosis;
        this.loginId = loginId;
        this.messageText = messageText;
        this.sender = sender;
    }
}
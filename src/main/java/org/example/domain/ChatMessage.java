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
    private long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false)
    private SenderType sender;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum SenderType {user, chatbot}

    public ChatMessage() {
    }

    public ChatMessage(Diagnosis diagnosis, String messageText, SenderType sender) {
        this.diagnosis = diagnosis;
        this.messageText = messageText;
        this.sender = sender;
    }
}
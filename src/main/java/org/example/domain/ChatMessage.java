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
    private Integer messageId;

    @Column(name = "diagnosis_id", nullable = false)
    private Integer diagnosisId;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false)
    private SenderType sender;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Enum
    public enum SenderType {user, chatbot}

    // Constructors
    public ChatMessage() {
    }

    public ChatMessage(Integer diagnosisId, String messageText, SenderType sender) {
        this.diagnosisId = diagnosisId;
        this.messageText = messageText;
        this.sender = sender;
    }


}

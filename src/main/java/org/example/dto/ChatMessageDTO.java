package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.domain.ChatMessage;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDTO {
    private String messageText;
    private ChatMessage.SenderType sender;
    private LocalDateTime createdAt;

    // ChatMessage 엔티티를 ChatMessageDto로 변환하는 생성자
    public ChatMessageDTO(ChatMessage message) {
        this.messageText = message.getMessageText();
        this.sender = message.getSender();
        this.createdAt = message.getCreatedAt();
    }
}
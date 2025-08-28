package org.example.service;

import org.example.domain.ChatMessage;
import org.example.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ChatBotService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String fastApiUrl = "http://localhost:8001";

    public String processChat(String userMessage, Integer diagnosisId) {
        try {
            // 1. 사용자 메시지 DB 저장
            ChatMessage userMsg = new ChatMessage(diagnosisId, userMessage, ChatMessage.SenderType.user);
            chatMessageRepository.save(userMsg);

            // 2. FastAPI로 AI 답변 요청
            String aiResponse = callFastAPI(userMessage, diagnosisId);

            // 3. AI 답변 DB 저장
            ChatMessage botMsg = new ChatMessage(diagnosisId, aiResponse, ChatMessage.SenderType.chatbot);
            chatMessageRepository.save(botMsg);

            return aiResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 현재 AI 상담 서비스에 문제가 있습니다.";
        }
    }

    private String callFastAPI(String message, Integer diagnosisId) {
        Map<String, Object> requestData = Map.of(
                "message", message,
                "diagnosis_id", diagnosisId
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    fastApiUrl + "/chat",
                    requestData,
                    Map.class
            );

            return (String) response.getBody().get("response");

        } catch (Exception e) {
            throw new RuntimeException("FastAPI 호출 실패: " + e.getMessage());
        }
    }

    // 채팅 히스토리 조회
    public List<ChatMessage> getChatHistory(Integer diagnosisId) {
        return chatMessageRepository.findByDiagnosisIdOrderByCreatedAt(diagnosisId);
    }
}
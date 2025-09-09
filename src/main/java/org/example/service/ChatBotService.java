package org.example.service;

import org.example.domain.ChatMessage;
import org.example.domain.Diagnosis;
import org.example.dto.ChatMessageDTO;
import org.example.repository.ChatMessageRepository;
import org.example.repository.DiagnosisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatBotService {

    private final ChatMessageRepository chatMessageRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final RestTemplate restTemplate;

    @Value("${fastapi.server.url}")
    private String fastApiUrl;

    @Autowired
    public ChatBotService(ChatMessageRepository chatMessageRepository, DiagnosisRepository diagnosisRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public String processChat(String userMessage, Integer diagnosisId, String loginId) {
        Diagnosis diagnosis = diagnosisRepository.findByIdWithCancer(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid diagnosis ID: " + diagnosisId));

        try {
            // 해당 사용자의 기존 메시지만 조회
            List<ChatMessage> existingMessages = chatMessageRepository
                    .findByDiagnosisAndLoginIdOrderByCreatedAtAsc(diagnosis, loginId);

            String contextMessage = userMessage;

            if (existingMessages.isEmpty() && diagnosis.getCancer() != null) {
                contextMessage = createInitialContextMessage(userMessage, diagnosis);
                System.out.println("진단 컨텍스트 추가됨: " + diagnosis.getCancer().getCancerName());
            }

            // loginId 포함해서 사용자 메시지 저장
            ChatMessage userMsg = new ChatMessage(diagnosis, loginId, userMessage, ChatMessage.SenderType.user);
            chatMessageRepository.save(userMsg);

            String aiResponse = callFastAPI(contextMessage, diagnosis, existingMessages);

            // loginId 포함해서 챗봇 메시지 저장
            ChatMessage botMsg = new ChatMessage(diagnosis, loginId, aiResponse, ChatMessage.SenderType.chatbot);
            chatMessageRepository.save(botMsg);

            return aiResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 현재 AI 상담 서비스에 문제가 있습니다.";
        }
    }

    private String callFastAPI(String message, Diagnosis diagnosis, List<ChatMessage> existingMessages) {
        // 대화 기록을 FastAPI 형식으로 변환
        List<Map<String, String>> historyData = existingMessages.stream()
                .map(msg -> Map.of(
                        "role", msg.getSender() == ChatMessage.SenderType.user ? "user" : "model",
                        "content", msg.getMessageText()
                ))
                .collect(Collectors.toList());

        Map<String, Object> requestData;
        if (diagnosis != null && diagnosis.getCancer() != null) {
            requestData = Map.of(
                    "message", message,
                    "diagnosis_id", diagnosis.getDiagnosisId(),
                    "cancer_name", diagnosis.getCancer().getCancerName(),
                    "cancer_description", Optional.ofNullable(diagnosis.getCancer().getDescription()).orElse(""),
                    "cancer_symptoms", Optional.ofNullable(diagnosis.getCancer().getSymptoms()).orElse(""),
                    "history", historyData
            );
        } else {
            requestData = Map.of(
                    "message", message,
                    "diagnosis_id", diagnosis.getDiagnosisId(),
                    "history", historyData
            );
        }

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

    // 반환 타입을 List<ChatMessageDto>로 변경
    public List<ChatMessageDTO> getChatHistory(Integer diagnosisId, String loginId) {
        try {
            Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid diagnosis ID: " + diagnosisId));

            List<ChatMessage> messages = chatMessageRepository.findByDiagnosisAndLoginIdOrderByCreatedAtAsc(diagnosis, loginId);

            // 조회한 엔티티 리스트를 DTO 리스트로 변환하여 반환
            return messages.stream()
                    .map(ChatMessageDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("getChatHistory 오류 - diagnosisId: " + diagnosisId + ", loginId: " + loginId);
            e.printStackTrace();
            // 빈 리스트 반환하여 오류 방지
            return List.of();
        }
    }

    public Diagnosis getDiagnosisInfo(Integer diagnosisId) {
        return diagnosisRepository.findByIdWithCancer(diagnosisId).orElse(null);
    }

    @Transactional
    public String startChatWithDiagnosis(Integer diagnosisId, String loginId) {
        Diagnosis diagnosis = getDiagnosisInfo(diagnosisId);
        if (diagnosis == null || diagnosis.getCancer() == null) {
            return "진단 정보를 찾을 수 없습니다.";
        }

        try {
            String userMessage = diagnosis.getCancer().getCancerName() + " 진단을 받았습니다. 상담을 받고 싶어요.";
            ChatMessage userMsg = new ChatMessage(diagnosis, loginId, userMessage, ChatMessage.SenderType.user);
            chatMessageRepository.save(userMsg);

            String contextMessage = createWelcomeContextMessage(diagnosis);
            String welcomeMessage = callFastAPI(contextMessage, diagnosis, List.of());

            ChatMessage welcomeMsg = new ChatMessage(diagnosis, loginId, welcomeMessage, ChatMessage.SenderType.chatbot);
            chatMessageRepository.save(welcomeMsg);

            return welcomeMessage;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FastAPI 호출 실패, 기본 메시지 사용: " + e.getMessage());
            String fallbackMessage = String.format(
                    "네, %s 진단 결과를 확인했습니다. 걱정이 많으시겠지만 함께 차근차근 알아보겠습니다. " +
                            "궁금한 점이나 걱정되는 부분이 있으시면 언제든 말씀해주세요.",
                    diagnosis.getCancer().getCancerName()
            );
            ChatMessage welcomeMsg = new ChatMessage(diagnosis, loginId, fallbackMessage, ChatMessage.SenderType.chatbot);
            chatMessageRepository.save(welcomeMsg);
            return fallbackMessage;
        }
    }

    @Transactional
    public List<ChatMessageDTO> chatWithDiagnosis(Integer diagnosisId, String loginId) {
        // 먼저 해당 사용자의 채팅 기록을 조회합니다.
        List<ChatMessageDTO> chatHistory = getChatHistory(diagnosisId, loginId);

        // 만약 채팅 기록이 없다면 (첫 상담이라면), 새로운 상담을 시작합니다.
        if (chatHistory.isEmpty()) {
            startChatWithDiagnosis(diagnosisId, loginId);
            // 새로운 상담이 시작되었으므로, 다시 채팅 기록을 조회하여 반환합니다.
            chatHistory = getChatHistory(diagnosisId, loginId);
        }

        return chatHistory;
    }

    /**
     * 새로운 상담 세션을 시작합니다.
     * 기존 기록을 삭제하고 새로운 환영 메시지를 생성합니다.
     */
    @Transactional
    public String startNewChatSession(Integer diagnosisId, String loginId) {
        clearChatHistory(diagnosisId, loginId);
        return startChatWithDiagnosis(diagnosisId, loginId);
    }

    /**
     * 특정 진단과 사용자의 채팅 기록을 모두 삭제합니다.
     */
    @Transactional
    public void clearChatHistory(Integer diagnosisId, String loginId) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid diagnosis ID: " + diagnosisId));

        chatMessageRepository.deleteByDiagnosisAndLoginId(diagnosis, loginId);
        System.out.println("채팅 기록이 초기화되었습니다. 진단 ID: " + diagnosisId + ", 사용자: " + loginId);
    }

    private String createInitialContextMessage(String userMessage, Diagnosis diagnosis) {
        return String.format(
                "[진단 정보] 환자의 '%s' 진단 결과가 나왔습니다. " +
                        "암 설명: %s\n주요 증상: %s\n\n" +
                        "이 진단 정보를 바탕으로 환자에게 적절한 상담을 제공해주세요.\n\n" +
                        "사용자 질문: %s",
                diagnosis.getCancer().getCancerName(),
                diagnosis.getCancer().getDescription(),
                diagnosis.getCancer().getSymptoms(),
                userMessage
        );
    }

    private String createWelcomeContextMessage(Diagnosis diagnosis) {
        return String.format(
                "[의료 AI 상담사로서 응답해주세요] " +
                        "환자가 '%s' 진단을 받았습니다. " +
                        "암 설명: %s " +
                        "주요 증상: %s " +
                        "환자가 상담을 요청했습니다. 따뜻하고 전문적인 톤으로 환영 인사와 함께 " +
                        "진단에 대한 걱정을 덜어주는 첫 메시지를 작성해주세요. " +
                        "150자 이내로 간단하게 작성해주세요.",
                diagnosis.getCancer().getCancerName(),
                Optional.ofNullable(diagnosis.getCancer().getDescription()).orElse("정보 없음"),
                Optional.ofNullable(diagnosis.getCancer().getSymptoms()).orElse("정보 없음")
        );
    }
}
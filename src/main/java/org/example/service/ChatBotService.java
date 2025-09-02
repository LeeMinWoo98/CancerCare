package org.example.service;

import org.example.domain.ChatMessage;
import org.example.domain.Diagnosis;
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
    public String processChat(String userMessage, Integer diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findByIdWithCancer(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid diagnosis ID: " + diagnosisId));

        try {
            List<ChatMessage> existingMessages = chatMessageRepository.findByDiagnosisOrderByCreatedAtAsc(diagnosis);
            String contextMessage = userMessage;

            if (existingMessages.isEmpty() && diagnosis.getCancer() != null) {
                contextMessage = createInitialContextMessage(userMessage, diagnosis);
                System.out.println("진단 컨텍스트 추가됨: " + diagnosis.getCancer().getCancerName());
            }

            ChatMessage userMsg = new ChatMessage(diagnosis, userMessage, ChatMessage.SenderType.user);
            chatMessageRepository.save(userMsg);

            String aiResponse = callFastAPI(contextMessage, diagnosis);

            ChatMessage botMsg = new ChatMessage(diagnosis, aiResponse, ChatMessage.SenderType.chatbot);
            chatMessageRepository.save(botMsg);

            return aiResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 현재 AI 상담 서비스에 문제가 있습니다.";
        }
    }

    private String callFastAPI(String message, Diagnosis diagnosis) {
        Map<String, Object> requestData;
        if (diagnosis != null && diagnosis.getCancer() != null) {
            requestData = Map.of(
                    "message", message,
                    "diagnosis_id", diagnosis.getDiagnosisId(),
                    "cancer_name", diagnosis.getCancer().getCancerName(),
                    "cancer_description", Optional.ofNullable(diagnosis.getCancer().getDescription()).orElse(""),
                    "cancer_symptoms", Optional.ofNullable(diagnosis.getCancer().getSymptoms()).orElse("")
            );
        } else {
            requestData = Map.of(
                    "message", message,
                    "diagnosis_id", diagnosis.getDiagnosisId()
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

    public List<ChatMessage> getChatHistory(Integer diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid diagnosis ID: " + diagnosisId));
        return chatMessageRepository.findByDiagnosisOrderByCreatedAtAsc(diagnosis);
    }

    public Diagnosis getDiagnosisInfo(Integer diagnosisId) {
        return diagnosisRepository.findByIdWithCancer(diagnosisId).orElse(null);
    }

    @Transactional
    public String startChatWithDiagnosis(Integer diagnosisId) {
        Diagnosis diagnosis = getDiagnosisInfo(diagnosisId);
        if (diagnosis == null || diagnosis.getCancer() == null) {
            return "진단 정보를 찾을 수 없습니다.";
        }

        try {
            String userMessage = diagnosis.getCancer().getCancerName() + " 진단을 받았습니다. 상담을 받고 싶어요.";
            ChatMessage userMsg = new ChatMessage(diagnosis, userMessage, ChatMessage.SenderType.user);
            chatMessageRepository.save(userMsg);

            String contextMessage = createWelcomeContextMessage(diagnosis);
            String welcomeMessage = callFastAPI(contextMessage, diagnosis);

            ChatMessage welcomeMsg = new ChatMessage(diagnosis, welcomeMessage, ChatMessage.SenderType.chatbot);
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
            ChatMessage welcomeMsg = new ChatMessage(diagnosis, fallbackMessage, ChatMessage.SenderType.chatbot);
            chatMessageRepository.save(welcomeMsg);
            return fallbackMessage;
        }
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
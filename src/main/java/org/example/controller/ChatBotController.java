package org.example.controller;

import org.example.dto.ChatMessageDTO;
import org.example.dto.ChatRequestDTO;
import org.example.dto.ChatResponseDTO;
import org.example.domain.ChatMessage;
import org.example.service.ChatBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chat")
public class ChatBotController {

    @Autowired
    private ChatBotService chatBotService;

    // 기본 채팅 페이지로 이동
    @GetMapping("/chatbot")
    public String chat() {
        return "chat";
    }

    /**
     * 🎯 특정 진단 ID 기반의 채팅 페이지로 이동 (사용자별로)
     */
    @GetMapping("/diagnosis/{diagnosisId}")
    public String chatWithDiagnosis(@PathVariable Integer diagnosisId, Model model, Authentication auth) {
        String loginId = auth.getName();
        model.addAttribute("diagnosisId", diagnosisId);

        // ★★★ 타입을 List<ChatMessageDTO>로 수정 ★★★
        List<ChatMessageDTO> chatHistory = chatBotService.getChatHistory(diagnosisId, loginId);
        if (chatHistory.isEmpty()) {
            // startChatWithDiagnosis는 반환값이 없으므로 그대로 두고,
            chatBotService.startChatWithDiagnosis(diagnosisId, loginId);
            // 다시 히스토리를 가져와 chatHistory 변수에 할당합니다.
            chatHistory = chatBotService.getChatHistory(diagnosisId, loginId);
        }

        model.addAttribute("chatHistory", chatHistory);
        return "chat";
    }

    /**
     * 🎯 채팅 메시지 전송 API (사용자 인증 추가)
     */
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<ChatResponseDTO> sendMessage(@RequestBody ChatRequestDTO request, Authentication auth) {
        try {
            String loginId = auth.getName(); // 🎯 현재 로그인한 사용자

            String aiResponse = chatBotService.processChat(
                    request.getMessage(),
                    request.getDiagnosisId(),
                    loginId  // 🎯 loginId 전달
            );

            return ResponseEntity.ok(new ChatResponseDTO(aiResponse, true));

        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponseDTO("오류가 발생했습니다.", false));
        }
    }

    /**
     * 🎯 채팅 히스토리 API (사용자별로)
     */
    @GetMapping("/history/{diagnosisId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Integer diagnosisId, Authentication auth) {
        try {
            String loginId = auth.getName();
            System.out.println("채팅 히스토리 요청 - diagnosisId: " + diagnosisId + ", loginId: " + loginId);
            
            List<ChatMessageDTO> history = chatBotService.getChatHistory(diagnosisId, loginId);
            System.out.println("조회된 히스토리 개수: " + history.size());
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            System.err.println("채팅 히스토리 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // 빈 리스트 반환
        }
    }

    /**
     * 🎯 새로운 상담 세션 시작 (사용자별로)
     */
    @PostMapping("/new-session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startNewChatSession(@RequestBody Map<String, Integer> request, Authentication auth) {
        try {
            String loginId = auth.getName(); // 🎯 현재 로그인한 사용자
            Integer diagnosisId = request.get("diagnosisId");

            // 🎯 해당 사용자의 새로운 상담 세션 시작
            String welcomeMessage = chatBotService.startNewChatSession(diagnosisId, loginId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "새로운 상담이 시작되었습니다.",
                    "welcomeMessage", welcomeMessage
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "새로운 상담 시작 중 오류가 발생했습니다."
            ));
        }
    }
}
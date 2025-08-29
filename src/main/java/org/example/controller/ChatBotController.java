package org.example.controller;

import org.example.dto.ChatRequest;
import org.example.dto.ChatResponse;
import org.example.domain.ChatMessage;
import org.example.service.ChatBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 특정 진단 ID 기반의 채팅 페이지로 이동.
     * 모델에 diagnosisId와 채팅 히스토리를 추가하여 뷰에 전달.
     * @param diagnosisId 경로 변수로 전달된 진단 ID.
     * @param model 뷰에 데이터를 전달하기 위한 모델 객체.
     * @return "chat" 뷰 이름.
     */
    @GetMapping("/diagnosis/{diagnosisId}")
    public String chatWithDiagnosis(@PathVariable Integer diagnosisId, Model model) {

        // 모델에 diagnosisId 속성 추가
        model.addAttribute("diagnosisId", diagnosisId);

        // 기존 채팅 히스토리 로드
        List<ChatMessage> chatHistory = chatBotService.getChatHistory(diagnosisId);
        model.addAttribute("chatHistory", chatHistory);

        return "chat";
    }

    // 채팅 메시지 전송 API
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        try {
            String aiResponse = chatBotService.processChat(
                    request.getMessage(),
                    request.getDiagnosisId()
            );

            return ResponseEntity.ok(new ChatResponse(aiResponse, true));

        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse("오류가 발생했습니다.", false));
        }
    }

    // 채팅 히스토리 API
    @GetMapping("/history/{diagnosisId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Integer diagnosisId) {
        List<ChatMessage> history = chatBotService.getChatHistory(diagnosisId);
        return ResponseEntity.ok(history);
    }
}
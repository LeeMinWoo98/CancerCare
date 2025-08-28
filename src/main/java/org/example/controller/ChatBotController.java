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

    @GetMapping("/chatbot")
    public String chat() {
        return "chat";
    }

    // 채팅 페이지 이동 (진단 기반)
    @GetMapping("/diagnosis/{diagnosisId}")
    public String chatWithDiagnosis(@PathVariable Integer diagnosisId, Model model) {
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
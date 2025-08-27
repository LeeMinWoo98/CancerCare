package org.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatBotController {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @GetMapping("/chatbot")
    public String chat() {
        return "chat";
    }

}
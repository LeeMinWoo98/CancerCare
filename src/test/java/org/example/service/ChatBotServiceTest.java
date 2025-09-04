package org.example.service;

import org.example.domain.ChatMessage;
import org.example.domain.Diagnosis;
import org.example.dto.ChatMessageDTO;
import org.example.domain.Cancer;
import org.example.repository.ChatMessageRepository;
import org.example.repository.DiagnosisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatBotServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @InjectMocks
    private ChatBotService chatBotService;

    @Test
    void getChatHistory() {
        // Given
        String loginId = "testuser";
        Integer diagnosisId = 1;

        Diagnosis testDiagnosis = new Diagnosis();
        testDiagnosis.setDiagnosisId(diagnosisId);

        ChatMessage fakeMessage = new ChatMessage(testDiagnosis, loginId, "안녕하세요", ChatMessage.SenderType.user);
        List<ChatMessage> fakeHistory = Collections.singletonList(fakeMessage);

        when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.of(testDiagnosis));
        when(chatMessageRepository.findByDiagnosisAndLoginIdOrderByCreatedAtAsc(testDiagnosis, loginId))
                .thenReturn(fakeHistory);

        // When
        List<ChatMessageDTO> result = chatBotService.getChatHistory(diagnosisId, loginId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("안녕하세요", result.get(0).getMessageText());
    }

    @Test
    void processChat() {}

    @Test
    void getDiagnosisInfo() {
        // Given
        Integer diagnosisId = 10;
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setDiagnosisId(diagnosisId);
        when(diagnosisRepository.findByIdWithCancer(diagnosisId)).thenReturn(Optional.of(diagnosis));

        // When
        Diagnosis result = chatBotService.getDiagnosisInfo(diagnosisId);

        // Then
        assertNotNull(result);
        assertEquals(diagnosisId, result.getDiagnosisId());
    }

    @Test
    void startChatWithDiagnosis() {
        // Given
        Integer diagnosisId = 20;
        String loginId = "user1";

        Cancer cancer = new Cancer();
        cancer.setCancerId(7);
        cancer.setCancerName("간암");

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setDiagnosisId(diagnosisId);
        diagnosis.setCancer(cancer);

        when(diagnosisRepository.findByIdWithCancer(diagnosisId)).thenReturn(Optional.of(diagnosis));

        // FastAPI 호출 실패를 유도하여 fallback 경로로 가도록 설정
        ReflectionTestUtils.setField(chatBotService, "fastApiUrl", "http://localhost:9");

        // When
        String message = chatBotService.startChatWithDiagnosis(diagnosisId, loginId);

        // Then
        assertNotNull(message);
        assertTrue(message.contains("간암"));
        verify(chatMessageRepository, times(1)).save(argThat(m -> m.getSender() == ChatMessage.SenderType.user));
        verify(chatMessageRepository, times(1)).save(argThat(m -> m.getSender() == ChatMessage.SenderType.chatbot));
    }

    @Test
    void chatWithDiagnosis() {
        // Given
        Integer diagnosisId = 30;
        String loginId = "user2";

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setDiagnosisId(diagnosisId);

        when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.of(diagnosis));

        // 첫 호출에서는 히스토리 없음, 두번째 호출에서는 1건 존재하도록 설정
        when(chatMessageRepository.findByDiagnosisAndLoginIdOrderByCreatedAtAsc(diagnosis, loginId))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(new ChatMessage(diagnosis, loginId, "hi", ChatMessage.SenderType.user)));

        // startChatWithDiagnosis 내부 FastAPI 실패 유도
        ReflectionTestUtils.setField(chatBotService, "fastApiUrl", "http://localhost:9");
        when(diagnosisRepository.findByIdWithCancer(diagnosisId)).thenReturn(Optional.of(diagnosis));

        // When
        List<ChatMessageDTO> result = chatBotService.chatWithDiagnosis(diagnosisId, loginId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void startNewChatSession() {
        // Given
        Integer diagnosisId = 40;
        String loginId = "user3";

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setDiagnosisId(diagnosisId);
        when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.of(diagnosis));
        when(diagnosisRepository.findByIdWithCancer(diagnosisId)).thenReturn(Optional.of(diagnosis));

        // FastAPI 실패 유도 (fallback)
        ReflectionTestUtils.setField(chatBotService, "fastApiUrl", "http://localhost:9");

        // When
        String result = chatBotService.startNewChatSession(diagnosisId, loginId);

        // Then
        assertNotNull(result);
        verify(chatMessageRepository, times(1)).deleteByDiagnosisAndLoginId(diagnosis, loginId);
    }

    @Test
    void clearChatHistory() {
        // Given
        Integer diagnosisId = 50;
        String loginId = "user4";
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setDiagnosisId(diagnosisId);
        when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.of(diagnosis));

        // When
        chatBotService.clearChatHistory(diagnosisId, loginId);

        // Then
        verify(chatMessageRepository, times(1)).deleteByDiagnosisAndLoginId(diagnosis, loginId);
    }
}
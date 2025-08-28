package org.example.repository;

import org.example.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    // 특정 진단의 채팅 히스토리 조회 (시간순 정렬)
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.diagnosisId = :diagnosisId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByDiagnosisIdOrderByCreatedAt(@Param("diagnosisId") Integer diagnosisId);

    // 특정 진단의 최근 N개 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.diagnosisId = :diagnosisId ORDER BY cm.createdAt DESC LIMIT :limit")
    List<ChatMessage> findRecentMessagesByDiagnosisId(@Param("diagnosisId") Integer diagnosisId, @Param("limit") int limit);
}
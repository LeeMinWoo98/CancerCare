package org.example.repository;

import org.example.domain.ChatMessage;
import org.example.domain.Diagnosis;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 🎯 특정 진단과 사용자의 채팅 기록을 시간 오름차순으로 조회
     */
    List<ChatMessage> findByDiagnosisAndLoginIdOrderByCreatedAtAsc(Diagnosis diagnosis, String loginId);

    /**
     * 🎯 특정 진단과 사용자의 최근 메시지 조회
     */
    List<ChatMessage> findByDiagnosisAndLoginIdOrderByCreatedAtDesc(Diagnosis diagnosis, String loginId, Pageable pageable);

    /**
     * 🎯 특정 진단과 사용자의 채팅 기록 삭제
     */
    @Modifying
    @Query("DELETE FROM ChatMessage cm WHERE cm.diagnosis = :diagnosis AND cm.loginId = :loginId")
    void deleteByDiagnosisAndLoginId(@Param("diagnosis") Diagnosis diagnosis, @Param("loginId") String loginId);
}


package org.example.repository;

import org.example.domain.ChatMessage;
import org.example.domain.Diagnosis;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 특정 진단에 대한 모든 채팅 기록을 시간 오름차순으로 조회합니다.
     * @param diagnosis 채팅 기록을 조회할 진단 객체
     * @return 시간순으로 정렬된 전체 채팅 메시지 목록
     */
    List<ChatMessage> findByDiagnosisOrderByCreatedAtAsc(Diagnosis diagnosis);

    /**
     * 특정 진단에 대한 최근 메시지를 시간 내림차순으로 조회합니다.
     * Pageable을 사용하여 가져올 개수를 동적으로 지정할 수 있습니다.
     * 예: PageRequest.of(0, 5)는 최신 5개를 의미합니다.
     * @param diagnosis 채팅 기록을 조회할 진단 객체
     * @param pageable 페이지 요청 정보
     * @return 시간 역순으로 정렬된 최근 채팅 메시지 목록
     */
    List<ChatMessage> findByDiagnosisOrderByCreatedAtDesc(Diagnosis diagnosis, Pageable pageable);

}


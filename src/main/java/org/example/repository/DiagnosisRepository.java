// DiagnosisRepository.java
package org.example.repository;

import org.example.domain.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Integer> {

    // 사용자별 진단 이력 조회
    List<Diagnosis> findByLoginIdOrderByDiagnosedAtDesc(String loginId);

    // 특정 진단 정보 조회 (ChatBot에서 사용)
    @Query("SELECT d FROM Diagnosis d JOIN FETCH d.cancer WHERE d.diagnosisId = :diagnosisId")
    Optional<Diagnosis> findByIdWithCancer(@Param("diagnosisId") Integer diagnosisId);
}
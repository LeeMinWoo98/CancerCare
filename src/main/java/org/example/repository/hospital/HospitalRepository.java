package org.example.repository.hospital;

import org.example.domain.hospital.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    /**
     * 카카오맵 API에서 제공하는 고유 ID로 병원을 찾습니다.
     * @param kakaoId 카카오맵 병원 ID
     * @return Optional<Hospital>
     */
    Optional<Hospital> findByKakaoId(String kakaoId);
}

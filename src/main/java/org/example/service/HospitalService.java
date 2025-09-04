package org.example.service;

import org.example.domain.hospital.Hospital;
import org.example.domain.User;
import org.example.domain.hospital.UserSavedHospital;
import org.example.dto.HospitalDto;
import org.example.dto.SaveHospitalsRequestDto;
import org.example.repository.hospital.HospitalRepository;
import org.example.repository.UserRepository;
import org.example.repository.hospital.UserSavedHospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class HospitalService {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final UserSavedHospitalRepository userSavedHospitalRepository;

    @Autowired
    public HospitalService(UserRepository userRepository, HospitalRepository hospitalRepository, UserSavedHospitalRepository userSavedHospitalRepository) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.userSavedHospitalRepository = userSavedHospitalRepository;
    }

    /**
     * 사용자가 선택한 병원 목록을 데이터베이스에 저장하고, 저장된 내역을 반환합니다.
     * 이 메서드는 트랜잭션으로 실행됩니다.
     * @param dto specialty(진료과목)와 hospital list를 포함하는 DTO
     * @return 저장된 UserSavedHospital 엔티티 목록
     */
    @Transactional
    public List<UserSavedHospital> saveHospitals(SaveHospitalsRequestDto dto) {
        // 1. 현재 로그인한 사용자 정보 가져오기
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + loginId));

        String specialty = dto.getSpecialty();
        List<UserSavedHospital> savedList = new ArrayList<>();

        // 2. 저장할 병원 목록을 순회
        for (HospitalDto hospitalDto : dto.getHospitals()) {

            // 3. DB에 이미 저장된 병원인지 'kakaoId'로 확인하고, 없으면 새로 생성하여 저장
            Hospital hospital = hospitalRepository.findByKakaoId(hospitalDto.getId())
                    .orElseGet(() -> {
                        Hospital newHospital = new Hospital(
                                hospitalDto.getId(),
                                hospitalDto.getName(),
                                hospitalDto.getAddress(),
                                hospitalDto.getPhone(),
                                hospitalDto.getUrl(),
                                hospitalDto.getX(),
                                hospitalDto.getY()
                        );
                        return hospitalRepository.save(newHospital);
                    });

            // 4. UserSavedHospital(조인 테이블)에 관계 정보 생성 및 저장
            UserSavedHospital savedInfo = new UserSavedHospital(user, hospital, specialty);
            userSavedHospitalRepository.save(savedInfo);
            savedList.add(savedInfo);

            System.out.println(user.getLoginId() + " 사용자가 '" + hospital.getName() + "' 병원을 저장했습니다.");
        }

        return savedList;
    }

    /**
     * 특정 사용자가 저장한 모든 병원 목록을 조회합니다.
     * @param loginId 사용자의 로그인 ID
     * @return 조회된 UserSavedHospital 엔티티 목록
     */
    @Transactional(readOnly = true) // 조회 기능이므로 readOnly=true 옵션으로 성능 최적화
    public List<UserSavedHospital> findSavedHospitalsByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .map(user -> userSavedHospitalRepository.findHospitalsByUserId(user.getId()))
                .orElse(Collections.emptyList()); // 사용자가 없으면 빈 리스트 반환
    }
}

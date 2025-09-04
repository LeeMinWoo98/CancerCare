package org.example.service;

import org.example.dto.SaveHospitalsRequestDto;
import org.springframework.stereotype.Service;

@Service
public class HospitalService {

    /**
     * 사용자가 선택한 병원 목록을 데이터베이스에 저장합니다.
     * @param dto specialty(진료과목)와 hospital list를 포함하는 DTO
     */
    public void saveHospitals(SaveHospitalsRequestDto dto) {
        // TODO: 데이터베이스에 병원 정보를 저장하는 로직을 구현합니다.
        // 1. dto.getSpecialty() 로 진료과목을 가져옵니다.
        // 2. dto.getHospitals() 로 병원 목록(List<Hospital>)을 가져옵니다.
        // 3. 각 Hospital 객체를 데이터베이스 엔티티로 변환하여 저장합니다.

        System.out.println("HospitalService: " + dto.getHospitals().size() + "개의 병원 정보를 저장합니다.");
        System.out.println("진료과목: " + dto.getSpecialty());
        dto.getHospitals().forEach(hospital -> {
            System.out.println("  -> " + hospital.getName() + " (" + hospital.getAddress() + ")");
        });
    }
}

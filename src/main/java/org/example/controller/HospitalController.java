package org.example.controller;

import org.example.domain.hospital.UserSavedHospital;
import org.example.dto.SaveHospitalsRequestDto;
import org.example.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Controller
@RequestMapping("/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @Value("${kakao.map.api.key:}")
    private String kakaoApiKey;

    @GetMapping("/find")
    public String findHospital(Model model) {
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        return "hospital/find";
    }

    @PostMapping("/api/save")
    public ResponseEntity<String> saveHospitals(@RequestBody SaveHospitalsRequestDto dto) {
        try {
            // 1. 서비스를 호출하고, 저장된 결과를 반환받습니다.
            List<UserSavedHospital> savedHospitals = hospitalService.saveHospitals(dto);

            // 2. 반환된 결과를 콘솔에 출력합니다.
            System.out.println("\n===== [HospitalController] DB 저장 결과 확인 =====");
            savedHospitals.forEach(savedInfo -> {
                System.out.println("  - 사용자: " + savedInfo.getUser().getLoginId());
                System.out.println("    병원명: " + savedInfo.getHospital().getName());
                System.out.println("    주소: " + savedInfo.getHospital().getAddress());
                System.out.println("    저장한 진료과목: " + savedInfo.getSpecialty());
                System.out.println("    저장 시각: " + savedInfo.getSavedAt());
                System.out.println("--------------------------------------------------");
            });
            System.out.println("==================================================\n");

            return ResponseEntity.ok("병원 정보가 성공적으로 저장되었습니다.");

        } catch (Exception e) {
            // 실제 프로덕션 코드에서는 로깅을 추가하는 것이 좋습니다.
            System.err.println("병원 정보 저장 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body("병원 정보 저장에 실패했습니다.");
        }
    }

    @GetMapping("/api/saved")
    @ResponseBody
    public ResponseEntity<List<UserSavedHospital>> getSavedHospitals() {
        try {
            String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
            List<UserSavedHospital> savedHospitals = hospitalService.findSavedHospitalsByLoginId(loginId);

            // 조회된 결과를 콘솔에 출력합니다.
            System.out.println("\n===== [HospitalController] DB 조회 결과 확인 =====");
            if (savedHospitals.isEmpty()) {
                System.out.println("  저장된 병원 정보가 없습니다.");
            } else {
                savedHospitals.forEach(savedInfo -> {
                    System.out.println("  - 사용자: " + savedInfo.getUser().getLoginId());
                    System.out.println("    병원명: " + savedInfo.getHospital().getName());
                    System.out.println("    주소: " + savedInfo.getHospital().getAddress());
                    System.out.println("    저장한 진료과목: " + savedInfo.getSpecialty());
                    System.out.println("    저장 시각: " + savedInfo.getSavedAt());
                    System.out.println("--------------------------------------------------");
                });
            }
            System.out.println("==================================================\n");

            return ResponseEntity.ok(savedHospitals);
        } catch (Exception e) {
            System.err.println("저장된 병원 목록 조회 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }
}

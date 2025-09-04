package org.example.controller;

import org.example.dto.SaveHospitalsRequestDto;
import org.example.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            hospitalService.saveHospitals(dto);
            return ResponseEntity.ok("병원 정보가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            // 실제 프로덕션 코드에서는 로깅을 추가하는 것이 좋습니다.
            System.err.println("병원 정보 저장 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body("병원 정보 저장에 실패했습니다.");
        }
    }
}

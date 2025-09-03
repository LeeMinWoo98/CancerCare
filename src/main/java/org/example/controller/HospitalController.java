package org.example.controller;

import org.example.dto.Hospital;
import org.example.service.HospitalService;
import org.example.service.AIRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private AIRecommendationService aiRecommendationService;

    @Value("${kakao.map.api.key:}")
    private String kakaoApiKey;

    @GetMapping("/find")
    public String findHospital(Model model) {
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        model.addAttribute("hospitals", hospitals);
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        return "hospital/find";
    }

    @GetMapping("/api/nearby")
    @ResponseBody
    public List<Hospital> getNearbyHospitals(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) String specialty) {

        return hospitalService.getNearbyHospitals(lat, lng, specialty);
    }

    @GetMapping("/api/ai-recommendation")
    @ResponseBody
    public Map<String, Object> getAIRecommendation(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String diagnosis) {

        List<Hospital> hospitals = hospitalService.getNearbyHospitals(lat, lng, specialty);
        List<Map<String, Object>> recommendations = aiRecommendationService.generateRecommendations(hospitals, diagnosis);
        Map<String, Object> insights = aiRecommendationService.getRecommendationInsights(hospitals);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("recommendations", recommendations);
        result.put("insights", insights);

        return result;
    }

    @PostMapping("/api/save")
    public ResponseEntity<String> saveHospitals(@RequestBody Map<String, Object> payload) {
        // payload.get("specialty") 로 진료과목을 가져올 수 있습니다.
        // payload.get("hospitals") 로 병원 목록(List<Map<String, Object>>)을 가져올 수 있습니다.

        // 여기에 실제 데이터베이스에 저장하는 로직을 구현합니다.
        System.out.println("저장할 데이터: " + payload);

        return ResponseEntity.ok("저장되었습니다.");
    }
}

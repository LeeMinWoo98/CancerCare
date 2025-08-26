package org.example.controller;

import org.example.model.Hospital;
import org.example.service.HospitalService;
import org.example.service.AIRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
}

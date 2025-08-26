package org.example.service;

import org.example.dto.Hospital;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {

    public List<Map<String, Object>> generateRecommendations(List<Hospital> hospitals, String userDiagnosis) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        if (hospitals.isEmpty()) {
            return recommendations;
        }

        // 1. 거리 기반 추천
        Map<String, Object> distanceRecommendation = createRecommendation(
                "거리 기반",
                getClosestHospital(hospitals),
                "가장 가까운 병원으로 빠른 접근이 가능합니다.",
                "distance"
        );
        recommendations.add(distanceRecommendation);

        // 2. 평점 기반 추천
        Map<String, Object> ratingRecommendation = createRecommendation(
                "평점 기반",
                getBestRatedHospital(hospitals),
                "가장 높은 평점을 받은 병원으로 의료 품질이 우수합니다.",
                "rating"
        );
        recommendations.add(ratingRecommendation);

        // 3. 진단 결과 기반 추천 (AI 로직)
        if (userDiagnosis != null && !userDiagnosis.isEmpty()) {
            Map<String, Object> diagnosisRecommendation = createDiagnosisBasedRecommendation(hospitals, userDiagnosis);
            if (diagnosisRecommendation != null) {
                recommendations.add(diagnosisRecommendation);
            }
        }

        // 4. 종합 점수 기반 추천 (AI 알고리즘)
        Map<String, Object> aiScoreRecommendation = createAIScoreRecommendation(hospitals);
        recommendations.add(aiScoreRecommendation);

        return recommendations;
    }

    private Map<String, Object> createRecommendation(String type, Hospital hospital, String reason, String algorithm) {
        Map<String, Object> recommendation = new HashMap<>();
        recommendation.put("type", type);
        recommendation.put("hospital", hospital);
        recommendation.put("reason", reason);
        recommendation.put("algorithm", algorithm);
        recommendation.put("confidence", calculateConfidence(hospital, algorithm));
        return recommendation;
    }

    private Hospital getClosestHospital(List<Hospital> hospitals) {
        return hospitals.stream()
                .min(Comparator.comparing(Hospital::getDistance))
                .orElse(hospitals.get(0));
    }

    private Hospital getBestRatedHospital(List<Hospital> hospitals) {
        return hospitals.stream()
                .max(Comparator.comparing(h -> Double.parseDouble(h.getRating())))
                .orElse(hospitals.get(0));
    }

    private Map<String, Object> createDiagnosisBasedRecommendation(List<Hospital> hospitals, String diagnosis) {
        // 진단 결과에 따른 전문병원 추천
        String specialty = determineSpecialty(diagnosis);

        List<Hospital> specialtyHospitals = hospitals.stream()
                .filter(h -> h.getSpecialty().contains(specialty))
                .collect(Collectors.toList());

        if (!specialtyHospitals.isEmpty()) {
            Hospital bestSpecialtyHospital = specialtyHospitals.stream()
                    .max(Comparator.comparing(h -> Double.parseDouble(h.getRating())))
                    .orElse(specialtyHospitals.get(0));

            return createRecommendation(
                    "진단 기반",
                    bestSpecialtyHospital,
                    diagnosis + " 진단에 특화된 " + specialty + " 전문병원입니다.",
                    "diagnosis"
            );
        }

        return null;
    }

    private String determineSpecialty(String diagnosis) {
        // 간단한 키워드 매칭 (실제로는 더 정교한 NLP 처리가 필요)
        diagnosis = diagnosis.toLowerCase();

        if (diagnosis.contains("암") || diagnosis.contains("종양") || diagnosis.contains("cancer")) {
            return "암전문";
        } else if (diagnosis.contains("피부") || diagnosis.contains("skin")) {
            return "피부과";
        } else if (diagnosis.contains("심장") || diagnosis.contains("heart")) {
            return "심장내과";
        } else if (diagnosis.contains("뇌") || diagnosis.contains("신경") || diagnosis.contains("brain")) {
            return "신경과";
        } else {
            return "종합병원";
        }
    }

    private Map<String, Object> createAIScoreRecommendation(List<Hospital> hospitals) {
        // AI 종합 점수 계산 (거리, 평점, 전문성 등을 종합적으로 고려)
        Hospital bestAIScoreHospital = hospitals.stream()
                .max(Comparator.comparing(this::calculateAIScore))
                .orElse(hospitals.get(0));

        double aiScore = calculateAIScore(bestAIScoreHospital);

        return createRecommendation(
                "AI 종합 추천",
                bestAIScoreHospital,
                "AI가 거리, 평점, 전문성 등을 종합적으로 분석한 최적의 병원입니다. (AI 점수: " + String.format("%.2f", aiScore) + ")",
                "ai_score"
        );
    }

    private double calculateAIScore(Hospital hospital) {
        // AI 점수 계산 알고리즘
        double distanceScore = Math.max(0, 100 - hospital.getDistance() * 10); // 거리 점수 (최대 100점)
        double ratingScore = Double.parseDouble(hospital.getRating()) * 20; // 평점 점수 (최대 100점)
        double specialtyScore = hospital.getSpecialty().contains("전문") ? 20 : 10; // 전문성 점수

        // 가중치 적용
        return distanceScore * 0.3 + ratingScore * 0.5 + specialtyScore * 0.2;
    }

    private double calculateConfidence(Hospital hospital, String algorithm) {
        // 추천 신뢰도 계산
        switch (algorithm) {
            case "distance":
                return Math.max(0.7, 1.0 - hospital.getDistance() / 50.0);
            case "rating":
                return Double.parseDouble(hospital.getRating()) / 5.0;
            case "diagnosis":
                return 0.9;
            case "ai_score":
                return Math.min(1.0, calculateAIScore(hospital) / 100.0);
            default:
                return 0.8;
        }
    }

    public Map<String, Object> getRecommendationInsights(List<Hospital> hospitals) {
        Map<String, Object> insights = new HashMap<>();

        // 통계 정보
        insights.put("totalHospitals", hospitals.size());
        insights.put("averageDistance", hospitals.stream().mapToDouble(Hospital::getDistance).average().orElse(0));
        insights.put("averageRating", hospitals.stream().mapToDouble(h -> Double.parseDouble(h.getRating())).average().orElse(0));

        // 전문성 분포
        Map<String, Long> specialtyDistribution = hospitals.stream()
                .collect(Collectors.groupingBy(Hospital::getSpecialty, Collectors.counting()));
        insights.put("specialtyDistribution", specialtyDistribution);

        // 거리별 분포
        Map<String, Long> distanceDistribution = hospitals.stream()
                .collect(Collectors.groupingBy(h -> {
                    double distance = h.getDistance();
                    if (distance <= 5) return "5km 이내";
                    else if (distance <= 10) return "5-10km";
                    else if (distance <= 20) return "10-20km";
                    else return "20km 이상";
                }, Collectors.counting()));
        insights.put("distanceDistribution", distanceDistribution);

        return insights;
    }
}

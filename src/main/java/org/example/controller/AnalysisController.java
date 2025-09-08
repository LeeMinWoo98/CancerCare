package org.example.controller;

import org.example.domain.Diagnosis;
import org.example.repository.CancerRepository;
import org.example.repository.DiagnosisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Controller
public class AnalysisController {

    @Autowired
    private CancerRepository cancerRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @GetMapping("/diagnosis")
    public String diagnosisPage() {
        return "diagnosis";
    }

    @PostMapping("/analyze/check")
    @ResponseBody
    public Map<String, Object> analyzeImage(@RequestParam("imageFile") MultipartFile file,
                                            Authentication authentication) {
        String scriptPath = "analyzer/check.py";
        return runAnalysis(file, scriptPath, authentication.getName());
    }

    private Map<String, Object> runAnalysis(MultipartFile file, String scriptPath, String loginId) {
        if (file.isEmpty()) {
            return Collections.singletonMap("error", "Please select a file to upload.");
        }

        Path savedPath = null;
        Process process = null;

        try {
            // 파일 저장
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));
            String savedFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            savedPath = Paths.get(uploadDir + savedFilename);
            Files.copy(file.getInputStream(), savedPath);

            // Python 실행
            ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, savedPath.toAbsolutePath().toString());
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            StringBuilder resultBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python Script Output: " + line);
                    resultBuilder.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return Collections.singletonMap("error", "Python 스크립트 실행 실패");
            }

            String fullResult = resultBuilder.toString();
            String predictionKey = "";

            if (fullResult.contains("Prediction Result:")) {
                predictionKey = fullResult.substring(fullResult.indexOf(":") + 1).trim();
            } else {
                return Collections.singletonMap("error", "스크립트 실행 중 오류가 발생했습니다.");
            }

            // ✨ 핵심 변경점: AI 결과를 cancers 테이블과 매핑
            String cancerName = mapPredictionToCancerName(predictionKey);

            // cancers 테이블에서 cancer_id 조회
            Integer cancerId = cancerRepository.findByCancerName(cancerName)
                    .map(cancer -> cancer.getCancerId())
                    .orElse(null);

            if (cancerId == null) {
                return Collections.singletonMap("error", "알 수 없는 암 종류입니다: " + cancerName);
            }

            // 🎯 진단 결과를 DB에 저장
            Diagnosis diagnosis = new Diagnosis(loginId, cancerId, savedPath.toString(), null);
            Diagnosis savedDiagnosis = diagnosisRepository.save(diagnosis);

            // 응답에 diagnosisId 포함
            return Map.of(
                    "prediction", cancerName,
                    "diagnosisId", savedDiagnosis.getDiagnosisId(),
                    "success", true
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", "분석 중 서버 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // 파일 정리
            if (process != null) {
                process.destroy();
            }
            try {
                if (savedPath != null && Files.exists(savedPath)) {
                    Files.delete(savedPath);
                }
            } catch (Exception e) {
                System.err.println("임시 파일 삭제에 실패했습니다: " + e.getMessage());
            }
        }
    }

    // AI 예측 결과를 cancers 테이블의 cancer_name으로 매핑
    private String mapPredictionToCancerName(String predictionKey) {
        if (predictionKey.toLowerCase().contains("liver")) {
            return "간암";
        } else if (predictionKey.toLowerCase().contains("lung")) {
            return "폐암";
        } else if (predictionKey.toLowerCase().contains("colon")) {
            return "대장암";
        } else if (predictionKey.toLowerCase().contains("breast")) {
            return "유방암";
        } else if (predictionKey.toLowerCase().contains("cervical")) {
            return "자궁경부암";
        } else if (predictionKey.toLowerCase().contains("stomach")) {
            return "위암";
        }
        return "알 수 없는 종류";
    }
}
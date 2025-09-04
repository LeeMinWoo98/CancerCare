package org.example.controller;

import org.example.domain.Diagnosis;
import org.example.domain.Cancer;
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
        // 현재 로그인한 사용자의 아이디를 runAnalysis 메소드로 전달합니다.
        return runAnalysis(file, scriptPath, authentication.getName());
    }

    /**
     * AI 분석을 실행하고, 결과를 DB에 저장한 후 주요 정보를 반환하는 메소드
     */
    private Map<String, Object> runAnalysis(MultipartFile file, String scriptPath, String loginId) {
        if (file.isEmpty()) {
            return Collections.singletonMap("error", "Please select a file to upload.");
        }

        Path savedPath = null;
        Process process = null;

        try {
            // 1. 업로드된 파일을 서버에 임시 저장
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));
            String savedFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            savedPath = Paths.get(uploadDir + savedFilename);
            Files.copy(file.getInputStream(), savedPath);

            // 2. 저장된 파일을 인자로 하여 Python 스크립트 실행
            ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, savedPath.toAbsolutePath().toString());
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            // Python 스크립트의 출력 결과 읽기
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

            // 3. AI 예측 결과를 DB와 연동하기 좋은 형태로 변환
            String cancerName = mapPredictionToCancerName(predictionKey);

            // 'cancers' 테이블에서 암 이름에 해당하는 ID를 조회
            Integer cancerId = cancerRepository.findByCancerName(cancerName)
                    .map(cancer -> cancer.getCancerId())
                    .orElse(null);

            if (cancerId == null) {
                return Collections.singletonMap("error", "알 수 없는 암 종류입니다: " + cancerName);
            }

            // 4. 최종 진단 결과를 'diagnoses' 테이블에 저장 (엔티티 구조에 맞게 설정)
            Diagnosis savedDiagnosis = saveDiagnosis(loginId, cancerId, savedPath.toString(), null);

            // 5. 프론트엔드(JavaScript)에 필요한 정보들을 Map 형태로 반환
            return Map.of(
                    "prediction", cancerName,
                    "diagnosisId", savedDiagnosis.getDiagnosisId(), // 챗봇 연동에 필요한 ID
                    "success", true
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", "분석 중 서버 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // 임시 파일 정리
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

    /**
     * AI 모델의 예측 키워드를 DB에 저장된 암 이름으로 매핑하는 헬퍼 메소드
     */
    private String mapPredictionToCancerName(String predictionKey) {
        String key = predictionKey.toLowerCase();
        if (key.contains("liver")) {
            return "간암";
        } else if (key.contains("lung")) {
            return "폐암";
        } else if (key.contains("colon")) {
            return "대장암";
        } else if (key.contains("breast")) {
            return "유방암";
        } else if (key.contains("cervical")) {
            return "자궁경부암";
        } else if (key.contains("stomach")) {
            return "위암";
        }
        return "알 수 없는 종류";
    }

    // 테스트 용이성을 위한 패키지-프라이빗 헬퍼: Diagnosis 생성 및 저장 책임 분리
    Diagnosis saveDiagnosis(String loginId, Integer cancerId, String imageUrl, Float certaintyScore) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setLoginId(loginId);
        Cancer cancer = new Cancer();
        cancer.setCancerId(cancerId);
        diagnosis.setCancer(cancer);
        diagnosis.setImageUrl(imageUrl);
        diagnosis.setCertaintyScore(certaintyScore);
        return diagnosisRepository.save(diagnosis);
    }
}
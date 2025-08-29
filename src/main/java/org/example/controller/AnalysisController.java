package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

    // 분석 페이지를 보여주는 역할
    @GetMapping("/diagnosis")
    public String diagnosisPage() {
        return "diagnosis";
    }

    // 분석 요청을 처리하고 데이터(JSON)만 반환
    @PostMapping("/analyze/check")
    @ResponseBody
    public Map<String, String> analyzeImage(@RequestParam("imageFile") MultipartFile file) {
        String scriptPath = "analyzer/check.py"; // 파이썬 스크립트 경로
        return runAnalysis(file, scriptPath);
    }

    /**
     * 분석을 실행하고 결과를 Map 형태로 반환하는 공통 메소드
     */
    private Map<String, String> runAnalysis(MultipartFile file, String scriptPath) {
        if (file.isEmpty()) {
            return Collections.singletonMap("error", "Please select a file to upload.");
        }

        Path savedPath = null;
        Process process = null;

        try {
            // ... (이미지 저장 및 파이썬 실행 부분은 동일) ...
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));
            String savedFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            savedPath = Paths.get(uploadDir + savedFilename);
            Files.copy(file.getInputStream(), savedPath);

            // Python 경로를 명시적으로 지정 (시스템에 따라 조정 필요)
            ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, savedPath.toAbsolutePath().toString());
            processBuilder.redirectErrorStream(true); // 에러 스트림도 함께 캡처
            process = processBuilder.start();

            StringBuilder resultBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();
            
            // 정상 출력 읽기
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python Script Output: " + line);
                    resultBuilder.append(line).append("\n");
                }
            }
            
            // 에러 출력 읽기 (redirectErrorStream(true) 사용 시 필요 없지만 안전장치)
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("Python Script Error: " + line);
                    errorBuilder.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            // 프로세스가 정상 종료되지 않은 경우
            if (exitCode != 0) {
                String errorMsg = errorBuilder.length() > 0 ? errorBuilder.toString() : "Python 스크립트 실행 실패";
                return Collections.singletonMap("error", "Python 스크립트 실행 실패 (종료 코드: " + exitCode + "). 오류: " + errorMsg);
            }

            String fullResult = resultBuilder.toString();
            String predictionKey = "";

            if (fullResult.contains("Prediction Result:")) {
                predictionKey = fullResult.substring(fullResult.indexOf(":") + 1).trim();
            } else {
                return Collections.singletonMap("error", "스크립트 실행 중 오류가 발생했습니다. 출력: " + fullResult);
            }
            
            // --- 👇 [수정된 부분] 예측 키워드를 한글 이름으로만 변환 ---
            String cancerName = "알 수 없는 종류";
            if (predictionKey.equalsIgnoreCase("ct_liver_cancer") || predictionKey.equalsIgnoreCase("liver_cancer")) {
                cancerName = "간암(CT)";
            } else if (predictionKey.equalsIgnoreCase("ct_lung_cancer")) {
                cancerName = "폐암(CT)";
            } else if (predictionKey.equalsIgnoreCase("ct_colon_cancer")) {
                cancerName = "대장암(CT)";
            } else if (predictionKey.equalsIgnoreCase("mri_liver_cancer")) {
                cancerName = "간암(MRI)";
            } else if (predictionKey.equalsIgnoreCase("mri_breast_cancer")) {
                cancerName = "유방암(MRI)";
            } else if (predictionKey.equalsIgnoreCase("mri_cervical_cancer")) {
                cancerName = "자궁경부암(MRI)";
            }
            // (다른 암 종류에 대한 변환 규칙 추가)

            // 최종 결과를 Map에 담아 반환
            return Collections.singletonMap("prediction", cancerName);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", "분속 중 서버 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // ... (파일 정리 부분은 동일) ...
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
}
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

    // MRI 분석 요청을 처리하고 데이터(JSON)만 반환
    @PostMapping("/analyze/mri")
    @ResponseBody
    public Map<String, String> analyzeMriImage(@RequestParam("imageFile") MultipartFile file) {
        String scriptPath = "analyzer/mri_check.py"; // 파이썬 프로젝트 폴더명 확인
        return runAnalysis(file, scriptPath);
    }

    // (향후 CT, 조직검사 PostMapping 추가 공간)
    // @PostMapping("/analyze/ct")
    // @ResponseBody
    // public Map<String, String> analyzeCtImage(@RequestParam("imageFile") MultipartFile file) {
    //     String scriptPath = "python_analyzer/ct_check.py";
    //     return runAnalysis(file, scriptPath);
    // }

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
            // 1. 업로드된 이미지 임시 저장
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));
            String savedFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            savedPath = Paths.get(uploadDir + savedFilename);
            Files.copy(file.getInputStream(), savedPath);

            // 2. 파이썬 스크립트 실행
            ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, savedPath.toAbsolutePath().toString());
            process = processBuilder.start();

            // 3. 파이썬 스크립트의 출력 결과 읽기
            StringBuilder resultBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // "Prediction Result:" 문자열이 포함된 라인을 찾으면, 그 줄을 저장하고 반복을 멈춤
                    if (line.contains("Prediction Result:")) {
                        resultBuilder.append(line);
                        break; 
                    }
                }
            }
            
            process.waitFor();

            // 4. 결과에서 예측값만 추출
            String fullResult = resultBuilder.toString();
            String prediction = "";

            if (!fullResult.isEmpty()) {
                prediction = fullResult.substring(fullResult.indexOf(":") + 1).trim();
            } else {
                // 파이썬 스크립트에서 "Prediction Result:" 라인을 출력하지 않은 경우
                return Collections.singletonMap("error", "Failed to get a valid prediction from the script.");
            }

            return Collections.singletonMap("prediction", prediction);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", "Error during analysis: " + e.getMessage());
        } finally {
            // 5. 프로세스와 임시 파일 최종 정리
            if (process != null) {
                process.destroy();
            }
            try {
                if (savedPath != null && Files.exists(savedPath)) {
                    Files.delete(savedPath);
                }
            } catch (Exception e) {
                System.err.println("Failed to delete temporary file: " + e.getMessage());
            }
        }
    }
}
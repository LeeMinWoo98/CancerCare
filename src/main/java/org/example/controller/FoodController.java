/*package org.example.controller;

import org.example.domain.CancerType;
import org.example.domain.DietRecommendation;
import org.example.domain.FoodItem;
import org.example.domain.FoodTableType;
import org.example.domain.User;
import org.example.domain.UserProfile;
import org.example.repository.UserRepository;
import org.example.service.FoodRecommendService;
import org.example.service.MyPageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Controller
@RequestMapping("/food")
public class FoodController {
    
    private final FoodRecommendService foodRecommendService;
    private final UserRepository userRepository;
    private final MyPageService myPageService;
    
    public FoodController(FoodRecommendService foodRecommendService, 
                         UserRepository userRepository, 
                         MyPageService myPageService) {
        this.foodRecommendService = foodRecommendService;
        this.userRepository = userRepository;
        this.myPageService = myPageService;
    }
    
    // 식단 추천 페이지
    @GetMapping("/recommend")
    public String recommendPage(Model model, Authentication authentication) {
        model.addAttribute("cancerTypes", CancerType.values());
        
        // 로그인한 사용자의 정보를 가져와서 모델에 추가
        if (authentication != null && authentication.isAuthenticated()) {
            String loginId = authentication.getName();
            try {
                User user = userRepository.findByLoginId(loginId).orElse(null);
                if (user != null) {
                    UserProfile userProfile = myPageService.getProfile(user.getId());
                    if (userProfile == null) {
                        userProfile = new UserProfile();
                        userProfile.setUserId(user.getId());
                    }
                    
                    // 나이 계산
                    Integer age = null;
                    if (user.getBirthdate() != null) {
                        age = Period.between(user.getBirthdate(), LocalDate.now()).getYears();
                    }
                    
                    // 성별 문자열 변환
                    String gender = null;
                    if (user.getGender() != null) {
                        switch (user.getGender()) {
                            case M: gender = "male"; break;
                            case F: gender = "female"; break;
                            default: gender = null; break;
                        }
                    }
                    
                    // 사용자 정보를 모델에 추가
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("name", user.getName());
                    userInfo.put("age", age);
                    userInfo.put("gender", gender);
                    userInfo.put("cancerType", userProfile.getCancerType() != null ? userProfile.getCancerType().name().toLowerCase() : null);
                    userInfo.put("stage", userProfile.getStage() != null ? userProfile.getStage().name().replace("S", "") : null);
                    userInfo.put("height", userProfile.getHeightCm());
                    userInfo.put("weight", userProfile.getWeightKg());
                    
                    model.addAttribute("userInfo", userInfo);
                }
            } catch (Exception e) {
                // 사용자 정보 조회 실패 시 로그만 출력하고 계속 진행
                System.out.println("사용자 정보 조회 실패: " + e.getMessage());
            }
        }
        
        return "food/recommend";
    }
    
    // JSON 형식 API: 통합 식단 추천
    @PostMapping("/api/recommend-json")
    @ResponseBody
    public Map<String, Object> recommendJson(@RequestBody Map<String, Object> request) {
        try {
            // 요청 데이터 파싱
            String cancerType = (String) request.get("cancer_type");
            Integer heightCm = (Integer) request.get("height_cm");
            Integer weightKg = (Integer) request.get("weight_kg");
            
            @SuppressWarnings("unchecked")
            List<String> allergies = (List<String>) request.getOrDefault("allergies", new ArrayList<>());
            @SuppressWarnings("unchecked")
            List<String> symptoms = (List<String>) request.getOrDefault("symptoms", new ArrayList<>());
            
            // CancerType 변환
            CancerType cancer = CancerType.fromString(cancerType);
            
            // BMI 계산 (칼로리 조정용)
            double bmi = 0;
            if (heightCm != null && weightKg != null && heightCm > 0 && weightKg > 0) {
                double heightM = heightCm / 100.0;
                bmi = weightKg / (heightM * heightM);
            }
            
            // 목표 칼로리 계산 (기본 650에서 BMI에 따라 조정)
            int targetKcal = 650;
            if (bmi > 0) {
                if (bmi < 18.5) {
                    targetKcal = 750; // 저체중
                } else if (bmi > 25) {
                    targetKcal = 550; // 과체중
                }
            }
            
            // 단백질 목표량 계산
            int proteinTarget = 30;
            if (weightKg != null) {
                proteinTarget = Math.max(25, (int)(weightKg * 0.8)); // 체중 1kg당 0.8g
            }
            
            // 증상에 따른 조정
            boolean hasAppetiteLoss = symptoms.contains("식욕부진");
            boolean hasDiarrhea = symptoms.contains("설사");
            boolean hasNausea = symptoms.contains("구토") || symptoms.contains("메스꺼움");
            
            // 메뉴 구성 (암 종류별 특화)
            Map<String, Object> menu = new HashMap<>();
            
            // 주식 (밥) - 암 종류별 조정
            String riceName = "현미밥";
            int riceKcal = 220;
            if (hasAppetiteLoss) {
                riceName = "죽 또는 부드러운 밥";
                riceKcal = 180;
            } else if (cancer == CancerType.STOMACH) {
                riceName = "흰쌀밥(소량)";
                riceKcal = 200;
            }
            menu.put("rice", Map.of("name", riceName, "kcal", riceKcal));
            
            // 국물
            String soupName = "두부미소국(저염)";
            int soupKcal = 120;
            if (hasNausea) {
                soupName = "맑은 채소국";
                soupKcal = 80;
            }
            menu.put("soup", Map.of("name", soupName, "kcal", soupKcal));
            
            // 반찬들
            List<Map<String, Object>> sides = new ArrayList<>();
            
            // 단백질 반찬
            if (!allergies.contains("seafood")) {
                sides.add(Map.of("name", "생선구이(저염)", "kcal", 180));
            } else if (!allergies.contains("egg")) {
                sides.add(Map.of("name", "계란찜", "kcal", 160));
            } else {
                sides.add(Map.of("name", "두부스테이크", "kcal", 150));
            }
            
            // 채소 반찬
            if (!hasDiarrhea) {
                sides.add(Map.of("name", "시금치나물", "kcal", 60));
                sides.add(Map.of("name", "버섯볶음", "kcal", 70));
            } else {
                sides.add(Map.of("name", "당근찜", "kcal", 50));
                sides.add(Map.of("name", "호박볶음", "kcal", 65));
            }
            
            menu.put("sides", sides);
            
            // 간식
            String snackName = "바나나+그릭요거트(무가당)";
            int snackKcal = 120;
            if (allergies.contains("milk")) {
                snackName = "바나나+견과류";
                snackKcal = 140;
            }
            if (allergies.contains("peanut") && snackName.contains("견과류")) {
                snackName = "바나나";
                snackKcal = 100;
            }
            menu.put("snack", Map.of("name", snackName, "kcal", snackKcal));
            
            // 총 칼로리 계산
            int totalKcal = riceKcal + soupKcal + snackKcal;
            for (Map<String, Object> side : sides) {
                totalKcal += (Integer) side.get("kcal");
            }
            
            // 추천 이유 생성
            List<String> reasoning = new ArrayList<>();
            reasoning.add("저염식 위주의 구성");
            reasoning.add("단백질 보강");
            
            if (!allergies.isEmpty()) {
                reasoning.add("알레르겐 제외");
            }
            
            if (hasAppetiteLoss) {
                reasoning.add("소화가 쉬운 음식 위주");
            }
            
            if (hasDiarrhea) {
                reasoning.add("장에 부담이 적은 음식");
            }
            
            // 경고사항
            List<String> warnings = new ArrayList<>();
            if (bmi > 0 && bmi < 18.5) {
                warnings.add("저체중이므로 칼로리 섭취량을 늘리세요");
            }
            if (hasNausea) {
                warnings.add("소량씩 자주 드시는 것을 권장합니다");
            }
            
            // 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("kcal_target", targetKcal);
            response.put("protein_target_g", proteinTarget);
            response.put("menu", menu);
            response.put("reasoning", reasoning);
            response.put("warnings", warnings);
            
            // 영양 정보 추정
            Map<String, Object> nutritionEstimate = new HashMap<>();
            nutritionEstimate.put("kcal_total", totalKcal);
            nutritionEstimate.put("protein_g_est", Math.min(proteinTarget + 5, 40));
            nutritionEstimate.put("sodium_level", "low");
            response.put("nutrition_estimate", nutritionEstimate);
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "식단 추천 중 오류가 발생했습니다: " + e.getMessage());
            return errorResponse;
        }
    }

    // API: 통합 식단 추천
    @GetMapping("/api/recommend")
    @ResponseBody
    public Map<String, Object> recommend(
            @RequestParam String cancer,
            @RequestParam(required = false) String foodType,
            @RequestParam(required = false, defaultValue = "0") int count,
            @RequestParam(required = false) String excludeIds) {
        
        try {
            CancerType cancerType = CancerType.fromString(cancer);
            List<Long> excludeIdList = parseExcludeIds(excludeIds);
            
            Map<String, Object> response = new HashMap<>();
            
            if (foodType == null || foodType.isEmpty()) {
                // 전체 식단 추천
                DietRecommendation recommendation = foodRecommendService.recommendDiet(cancerType);
                
                Map<String, Object> data = new HashMap<>();
                data.put("cancerType", recommendation.getCancerType().name());
                data.put("cancerLabel", recommendation.getCancerLabel());
                data.put("insufficient", recommendation.isInsufficient());
                
                if (recommendation.isInsufficient()) {
                    data.put("message", recommendation.getMessage());
                } else {
                    Map<String, Object> menu = new HashMap<>();
                    menu.put("sideDishes", recommendation.getSideDishes());
                    menu.put("soup", recommendation.getSoup());
                    menu.put("rice", recommendation.getRice());
                    menu.put("snack", recommendation.getSnack());
                    data.put("menu", menu);
                }
                
                response.put("success", true);
                response.put("data", data);
                
            } else {
                // 부분 추천
                FoodTableType tableType = FoodTableType.fromString(foodType);
                int requestCount = count > 0 ? count : tableType.getRecommendCount();
                
                List<FoodItem> items = foodRecommendService.recommendByType(
                    cancerType, tableType, requestCount, excludeIdList
                );
                
                Map<String, Object> data = new HashMap<>();
                data.put("cancerType", cancerType.name());
                data.put("cancerLabel", cancerType.getDisplayName());
                data.put("insufficient", items.size() < requestCount);
                data.put("items", items);
                
                response.put("success", true);
                response.put("data", data);
            }
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "오류가 발생했습니다: " + e.getMessage());
            return errorResponse;
        }
    }
    
    private List<Long> parseExcludeIds(String excludeIds) {
        if (excludeIds == null || excludeIds.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> ids = new ArrayList<>();
        try {
            String[] parts = excludeIds.split(",");
            for (String part : parts) {
                ids.add(Long.parseLong(part.trim()));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid excludeIds format: " + excludeIds);
        }
        return ids;
    }
}
*/
package org.example.controller;

import org.example.domain.CancerType;
import org.example.domain.DietRecommendation;
import org.example.domain.FoodItem;
import org.example.domain.FoodTableType;
import org.example.service.FoodRecommendService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/food")
public class FoodController {
    
    private final FoodRecommendService foodRecommendService;
    
    public FoodController(FoodRecommendService foodRecommendService) {
        this.foodRecommendService = foodRecommendService;
    }
    
    // 식단 추천 페이지
    @GetMapping("/recommend")
    public String recommendPage(Model model) {
        model.addAttribute("cancerTypes", CancerType.values());
        return "food/recommend";
    }
    
    // API: 통합 식단 추천
    @GetMapping("/api/recommend")
    @ResponseBody
    public Map<String, Object> recommend(
            @RequestParam("cancer") String cancer,
            @RequestParam(name = "foodType", required = false) String foodType,
            @RequestParam(name = "count", required = false, defaultValue = "0") int count,
            @RequestParam(name = "excludeIds", required = false) String excludeIds) {
        
        try {
            CancerType cancerType = CancerType.fromString(cancer);
            List<Long> excludeIdList = parseExcludeIds(excludeIds);
            
            Map<String, Object> response = new HashMap<>();
            
            if (foodType == null || foodType.isEmpty()) {
                // 전체 식단 추천
                DietRecommendation recommendation = foodRecommendService.recommendDiet(cancerType);
                
                Map<String, Object> data = new HashMap<>();
                data.put("cancerType", recommendation.getCancerType().name());
                data.put("cancerLabel", recommendation.getCancerLabel());
                data.put("insufficient", recommendation.isInsufficient());
                
                if (recommendation.isInsufficient()) {
                    data.put("message", recommendation.getMessage());
                } else {
                    Map<String, Object> menu = new HashMap<>();
                    menu.put("sideDishes", recommendation.getSideDishes());
                    menu.put("soup", recommendation.getSoup());
                    menu.put("rice", recommendation.getRice());
                    menu.put("snack", recommendation.getSnack());
                    data.put("menu", menu);
                }
                
                response.put("success", true);
                response.put("data", data);
                
            } else {
                // 부분 추천
                FoodTableType tableType = FoodTableType.fromString(foodType);
                int requestCount = count > 0 ? count : tableType.getRecommendCount();
                
                List<FoodItem> items = foodRecommendService.recommendByType(
                    cancerType, tableType, requestCount, excludeIdList
                );
                
                Map<String, Object> data = new HashMap<>();
                data.put("cancerType", cancerType.name());
                data.put("cancerLabel", cancerType.getDisplayName());
                data.put("insufficient", items.size() < requestCount);
                data.put("items", items);
                
                response.put("success", true);
                response.put("data", data);
            }
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "오류가 발생했습니다: " + e.getMessage());
            return errorResponse;
        }
    }
    
    private List<Long> parseExcludeIds(String excludeIds) {
        if (excludeIds == null || excludeIds.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> ids = new ArrayList<>();
        try {
            String[] parts = excludeIds.split(",");
            for (String part : parts) {
                ids.add(Long.parseLong(part.trim()));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid excludeIds format: " + excludeIds);
        }
        return ids;
    }
}
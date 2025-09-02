/*package org.example.controller;

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
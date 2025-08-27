package org.example.service;

import org.example.domain.CancerType;
import org.example.domain.DietRecommendation;
import org.example.domain.FoodItem;
import org.example.domain.FoodTableType;
import org.example.repository.FoodRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FoodRecommendService {
    
    private final FoodRepository foodRepository;
    
    public FoodRecommendService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }
    
    public DietRecommendation recommendDiet(CancerType cancerType) {
        // 각 카테고리별 추천 시도
        List<FoodItem> sideDishes = foodRepository.findRandomFoodsByType(
            FoodTableType.SIDE_DISH, cancerType, 3, Collections.emptyList()
        );
        
        List<FoodItem> soupList = foodRepository.findRandomFoodsByType(
            FoodTableType.SOUPS, cancerType, 1, Collections.emptyList()
        );
        FoodItem soup = soupList.isEmpty() ? null : soupList.get(0);
        
        List<FoodItem> riceList = foodRepository.findRandomFoodsByType(
            FoodTableType.RICE, cancerType, 1, Collections.emptyList()
        );
        FoodItem rice = riceList.isEmpty() ? null : riceList.get(0);
        
        List<FoodItem> snackList = foodRepository.findRandomFoodsByType(
            FoodTableType.SNACK, cancerType, 1, Collections.emptyList()
        );
        FoodItem snack = snackList.isEmpty() ? null : snackList.get(0);
        
        // 데이터 부족 여부 체크
        boolean insufficient = soup == null || rice == null || snack == null || sideDishes.size() < 3;
        
        return new DietRecommendation(cancerType, sideDishes, soup, rice, snack, insufficient);
    }
    
    public List<FoodItem> recommendByType(CancerType cancerType, FoodTableType foodType, 
                                         int count, List<Long> excludeIds) {
        return foodRepository.findRandomFoodsByType(foodType, cancerType, count, excludeIds);
    }
}

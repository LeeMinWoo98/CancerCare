package org.example.service;

import org.example.domain.CancerType;
import org.example.domain.DietRecommendation;
import org.example.domain.FoodItem;
import org.example.domain.FoodTableType;
import org.example.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodRecommendServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodRecommendService foodRecommendService;

    @Test
    void recommendDiet_buildsRecommendation_andDetectsInsufficient() {
        // Given
        CancerType cancerType = CancerType.LIVER;

        List<FoodItem> sides = List.of(new FoodItem(), new FoodItem(), new FoodItem());
        List<FoodItem> soups = List.of(new FoodItem());
        List<FoodItem> rice = List.of(new FoodItem());
        List<FoodItem> snack = List.of(new FoodItem());

        when(foodRepository.findRandomFoodsByType(FoodTableType.SIDE_DISH, cancerType, 3, Collections.emptyList()))
                .thenReturn(sides);
        when(foodRepository.findRandomFoodsByType(FoodTableType.SOUPS, cancerType, 1, Collections.emptyList()))
                .thenReturn(soups);
        when(foodRepository.findRandomFoodsByType(FoodTableType.RICE, cancerType, 1, Collections.emptyList()))
                .thenReturn(rice);
        when(foodRepository.findRandomFoodsByType(FoodTableType.SNACK, cancerType, 1, Collections.emptyList()))
                .thenReturn(snack);

        // When
        DietRecommendation rec = foodRecommendService.recommendDiet(cancerType);

        // Then
        assertNotNull(rec);
        assertEquals(cancerType, rec.getCancerType());
        assertEquals(3, rec.getSideDishes().size());
        assertNotNull(rec.getSoup());
        assertNotNull(rec.getRice());
        assertNotNull(rec.getSnack());
        assertFalse(rec.isInsufficient());
    }

    @Test
    void recommendByType_delegatesToRepository() {
        CancerType cancerType = CancerType.LUNG;
        when(foodRepository.findRandomFoodsByType(FoodTableType.RICE, cancerType, 2, List.of(1L)))
                .thenReturn(List.of(new FoodItem(), new FoodItem()));

        List<FoodItem> result = foodRecommendService.recommendByType(cancerType, FoodTableType.RICE, 2, List.of(1L));
        assertEquals(2, result.size());
        verify(foodRepository, times(1)).findRandomFoodsByType(FoodTableType.RICE, cancerType, 2, List.of(1L));
    }
}

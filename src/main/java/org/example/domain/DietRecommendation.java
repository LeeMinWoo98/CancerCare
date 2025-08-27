package org.example.domain;

import java.util.List;

public class DietRecommendation {
    private CancerType cancerType;
    private String cancerLabel;
    private List<FoodItem> sideDishes;
    private FoodItem soup;
    private FoodItem rice;
    private FoodItem snack;
    private boolean insufficient;
    private String message;
    
    public DietRecommendation() {}
    
    public DietRecommendation(CancerType cancerType, List<FoodItem> sideDishes, 
                            FoodItem soup, FoodItem rice, FoodItem snack, boolean insufficient) {
        this.cancerType = cancerType;
        this.cancerLabel = cancerType.getDisplayName();
        this.sideDishes = sideDishes;
        this.soup = soup;
        this.rice = rice;
        this.snack = snack;
        this.insufficient = insufficient;
        if (insufficient) {
            this.message = "해당 암 타입에 추천 가능한 음식이 부족합니다.";
        }
    }
    
    // Getters and Setters
    public CancerType getCancerType() {
        return cancerType;
    }
    
    public void setCancerType(CancerType cancerType) {
        this.cancerType = cancerType;
    }
    
    public String getCancerLabel() {
        return cancerLabel;
    }
    
    public void setCancerLabel(String cancerLabel) {
        this.cancerLabel = cancerLabel;
    }
    
    public List<FoodItem> getSideDishes() {
        return sideDishes;
    }
    
    public void setSideDishes(List<FoodItem> sideDishes) {
        this.sideDishes = sideDishes;
    }
    
    public FoodItem getSoup() {
        return soup;
    }
    
    public void setSoup(FoodItem soup) {
        this.soup = soup;
    }
    
    public FoodItem getRice() {
        return rice;
    }
    
    public void setRice(FoodItem rice) {
        this.rice = rice;
    }
    
    public FoodItem getSnack() {
        return snack;
    }
    
    public void setSnack(FoodItem snack) {
        this.snack = snack;
    }
    
    public boolean isInsufficient() {
        return insufficient;
    }
    
    public void setInsufficient(boolean insufficient) {
        this.insufficient = insufficient;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

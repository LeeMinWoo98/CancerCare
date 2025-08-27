package org.example.domain;

public enum FoodTableType {
    SIDE_DISH("side_dish", "sidedish_name", 3),
    SOUPS("soups", "soup_name", 1),
    RICE("rice", "rice_name", 1),
    SNACK("snack", "snack_name", 1);
    
    private final String tableName;
    private final String nameColumn;
    private final int recommendCount;
    
    FoodTableType(String tableName, String nameColumn, int recommendCount) {
        this.tableName = tableName;
        this.nameColumn = nameColumn;
        this.recommendCount = recommendCount;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public String getNameColumn() {
        return nameColumn;
    }
    
    public int getRecommendCount() {
        return recommendCount;
    }
    
    public static FoodTableType fromString(String value) {
        switch (value.toLowerCase()) {
            case "sidedish":
            case "side_dish":
                return SIDE_DISH;
            case "soup":
            case "soups":
                return SOUPS;
            case "rice":
                return RICE;
            case "snack":
                return SNACK;
            default:
                throw new IllegalArgumentException("Unknown food type: " + value);
        }
    }
}

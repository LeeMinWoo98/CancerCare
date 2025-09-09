package org.example.domain;

public enum CancerType {
    LUNG("Lung", "폐암"),
    LIVER("Liver", "간암"), 
    COLON("Colon", "대장암"),
    STOMACH("Stomach", "위암"),
    CERVICAL("Cervical", "자궁경부암"),
    BREAST("Breast", "유방암");
    
    private final String columnName;
    private final String displayName;
    
    CancerType(String columnName, String displayName) {
        this.columnName = columnName;
        this.displayName = displayName;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static CancerType fromString(String value) {
        for (CancerType type : CancerType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown cancer type: " + value);
    }
}

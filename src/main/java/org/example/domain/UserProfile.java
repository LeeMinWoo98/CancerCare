package org.example.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    private Long userId; // PK == FK(app_users.id)

    @Enumerated(EnumType.STRING)
    @Column(name = "cancer_type")
    private CancerType cancerType; // NULL 허용

    @Enumerated(EnumType.STRING)
    @Column(name = "stage")
    private Stage stage; // NULL 허용

    @Column(name = "height_cm")
    private Integer heightCm; // NULL 허용

    @Column(name = "weight_kg")
    private Integer weightKg; // NULL 허용

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public UserProfile() {}

    public UserProfile(Long userId) {
        this.userId = userId;
    }

    // Getters/Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public CancerType getCancerType() { return cancerType; }
    public void setCancerType(CancerType cancerType) { this.cancerType = cancerType; }

    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }

    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

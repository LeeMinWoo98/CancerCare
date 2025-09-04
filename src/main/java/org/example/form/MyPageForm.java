package org.example.form;

import org.example.domain.CancerType;
import org.example.domain.Stage;
import org.example.domain.User;

import java.time.LocalDate;

public class MyPageForm {
    // 기본 정보
    private String name;
    private String email;
    private User.Gender gender;
    private String loginId;
    private LocalDate birthdate;
    
    // 의료 정보
    private CancerType cancerType; // nullable
    private Stage stage; // nullable
    private Integer heightCm; // nullable
    private Integer weightKg; // nullable

    // 기본 정보 getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public User.Gender getGender() { return gender; }
    public void setGender(User.Gender gender) { this.gender = gender; }
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
    
    // 의료 정보 getters/setters
    public CancerType getCancerType() { return cancerType; }
    public void setCancerType(CancerType cancerType) { this.cancerType = cancerType; }
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }
}

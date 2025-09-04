package org.example.dto;

import org.example.domain.CancerType;
import org.example.domain.Stage;
import org.example.domain.User;
import java.time.LocalDate;

public class UserProfileView {
    // 사용자 기본 정보
    private String name;
    private String loginId;
    private String email;
    private User.Gender gender;
    private LocalDate birthdate;
    private int age;
    
    // 프로필 정보
    private CancerType cancerType;
    private Stage stage;
    private Integer heightCm;
    private Integer weightKg;

    // 사용자 기본 정보 getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public User.Gender getGender() { return gender; }
    public void setGender(User.Gender gender) { this.gender = gender; }
    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    // 프로필 정보 getters/setters
    public CancerType getCancerType() { return cancerType; }
    public void setCancerType(CancerType cancerType) { this.cancerType = cancerType; }
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }
}

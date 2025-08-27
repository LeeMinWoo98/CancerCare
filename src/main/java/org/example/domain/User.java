package org.example.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "AppUser")
@Table(name = "app_users", catalog = "human_team6")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String loginId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender = Gender.N;

    @Column(nullable = false)
    private LocalDate birthdate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Gender { M, F, N }

    // Constructors
    public User() {}

    public User(String loginId, String name, String email, String password, Gender gender, LocalDate birthdate) {
        this.loginId = loginId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.birthdate = birthdate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package org.example.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "diagnoses")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_id")
    private Integer diagnosisId;

    @Column(name = "login_id", nullable = false)
    private String loginId;

    @Column(name = "cancer_id", nullable = false)
    private Integer cancerId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "certainty_score")
    private Float certaintyScore;

    @Column(name = "diagnosed_at", nullable = false)
    private LocalDateTime diagnosedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancer_id", insertable = false, updatable = false)
    private Cancer cancer;

    // 생성자
    public Diagnosis() {}

    public Diagnosis(String loginId, Integer cancerId, String imageUrl, Float certaintyScore) {
        this.loginId = loginId;
        this.cancerId = cancerId;
        this.imageUrl = imageUrl;
        this.certaintyScore = certaintyScore;
    }
}
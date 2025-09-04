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

    @Column(name = "login_id", nullable = false, length = 30)
    private String loginId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancer_id", nullable = false)
    private Cancer cancer;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "certainty_score")
    private Float certaintyScore;

    @Column(name = "diagnosed_at", nullable = false)
    private LocalDateTime diagnosedAt = LocalDateTime.now();
}
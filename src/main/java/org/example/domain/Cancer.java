package org.example.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cancers")
public class Cancer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancer_id")
    private Integer cancerId;

    @Column(name = "cancer_name", nullable = false)
    private String cancerName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    // 생성자
    public Cancer() {}

    public Cancer(String cancerName, String description, String symptoms) {
        this.cancerName = cancerName;
        this.description = description;
        this.symptoms = symptoms;
    }
}
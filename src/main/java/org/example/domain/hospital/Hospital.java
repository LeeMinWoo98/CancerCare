package org.example.domain.hospital;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "hospital")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long id;

    @Column(name = "kakao_id", unique = true, nullable = false)
    private String kakaoId; // 카카오맵 API 가 제공하는 병원 ID

    @Column(nullable = false)
    private String name; // 병원 이름

    @Column(nullable = false)
    private String address; // 주소

    private String phone; // 전화번호

    private String url; // 상세 정보 URL

    @Column(name = "pos_x", nullable = false)
    private String x; // 경도 (longitude)

    @Column(name = "pos_y", nullable = false)
    private String y; // 위도 (latitude)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 생성자
    public Hospital(String kakaoId, String name, String address, String phone, String url, String x, String y) {
        this.kakaoId = kakaoId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.url = url;
        this.x = x;
        this.y = y;
    }
}

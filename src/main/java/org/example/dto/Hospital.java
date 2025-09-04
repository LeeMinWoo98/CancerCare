package org.example.dto;

import lombok.Data;

@Data
public class Hospital {
    private String id;       // 병원 ID (from Kakao API)
    private String name;     // 병원 이름
    private String address;  // 주소
    private String phone;    // 전화번호
    private String url;      // 상세 정보 URL
    private String x;        // 경도(longitude)
    private String y;        // 위도(latitude)
}

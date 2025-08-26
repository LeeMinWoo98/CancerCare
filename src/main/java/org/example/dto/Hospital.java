package org.example.dto;

import lombok.Data;

@Data
public class Hospital {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String website;
    private Double latitude;
    private Double longitude;
    private String specialty;
    private String rating;
    private String operatingHours;
    private String description;
    private Double distance; // 사용자 위치로부터의 거리
}

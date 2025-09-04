package org.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaveHospitalsRequestDto {
    private String specialty;
    private List<HospitalDto> hospitals;
}

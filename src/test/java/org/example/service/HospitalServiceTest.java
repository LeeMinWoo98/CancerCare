package org.example.service;

import org.example.dto.Hospital;
import org.example.dto.SaveHospitalsRequestDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HospitalServiceTest {

    @Test
    void saveHospitals_printsInfo_withoutException() {
        HospitalService service = new HospitalService();
        SaveHospitalsRequestDto dto = new SaveHospitalsRequestDto();
        dto.setSpecialty("내과");
        Hospital h1 = new Hospital();
        h1.setName("H1");
        h1.setAddress("addr1");
        h1.setPhone("tel1");
        Hospital h2 = new Hospital();
        h2.setName("H2");
        h2.setAddress("addr2");
        h2.setPhone("tel2");
        dto.setHospitals(List.of(h1, h2));

        assertDoesNotThrow(() -> service.saveHospitals(dto));
    }
}

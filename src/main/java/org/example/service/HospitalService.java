package org.example.service;

import org.example.model.Hospital;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalService {

    private List<Hospital> hospitals = new ArrayList<>();

    @PostConstruct
    public void loadHospitalsFromCSV() {
        try {
            ClassPathResource resource = new ClassPathResource("data/hospitals.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 헤더 스킵
                }

                String[] data = line.split(",");
                if (data.length >= 11) {
                    Hospital hospital = new Hospital();
                    hospital.setId(Long.parseLong(data[0]));
                    hospital.setName(data[1]);
                    hospital.setAddress(data[2]);
                    hospital.setPhone(data[3]);
                    hospital.setWebsite(data[4]);
                    hospital.setLatitude(Double.parseDouble(data[5]));
                    hospital.setLongitude(Double.parseDouble(data[6]));
                    hospital.setSpecialty(data[7]);
                    hospital.setRating(data[8]);
                    hospital.setOperatingHours(data[9]);
                    hospital.setDescription(data[10]);

                    hospitals.add(hospital);
                }
            }

            reader.close();
            System.out.println("병원 데이터 로드 완료: " + hospitals.size() + "개");

        } catch (IOException e) {
            System.err.println("병원 데이터 로드 실패: " + e.getMessage());
            // CSV 로드 실패 시 기본 데이터 사용
            loadDefaultHospitals();
        }
    }

    private void loadDefaultHospitals() {
        // 기본 병원 데이터 (CSV 로드 실패 시 사용)
        hospitals.add(createHospital(1L, "서울대학교병원", "서울특별시 종로구 대학로 101", "02-2072-0114",
                "https://www.snuh.org", 37.5665, 126.9780, "종합병원", "4.8", "24시간", "국내 최고의 종합병원"));

        hospitals.add(createHospital(2L, "연세대학교세브란스병원", "서울특별시 서대문구 연세로 50-1", "02-2228-5800",
                "https://www.severance.healthcare", 37.5642, 126.9369, "종합병원", "4.7", "24시간", "세계적인 의료진"));

        hospitals.add(createHospital(3L, "국립암센터", "경기도 고양시 일산동구 일산로 323", "031-920-0114",
                "https://www.ncc.re.kr", 37.6584, 126.7698, "암전문병원", "4.9", "09:00-18:00", "국내 최고의 암 전문병원"));
    }

    private Hospital createHospital(Long id, String name, String address, String phone,
                                    String website, Double lat, Double lng, String specialty,
                                    String rating, String hours, String description) {
        Hospital hospital = new Hospital();
        hospital.setId(id);
        hospital.setName(name);
        hospital.setAddress(address);
        hospital.setPhone(phone);
        hospital.setWebsite(website);
        hospital.setLatitude(lat);
        hospital.setLongitude(lng);
        hospital.setSpecialty(specialty);
        hospital.setRating(rating);
        hospital.setOperatingHours(hours);
        hospital.setDescription(description);
        return hospital;
    }

    public List<Hospital> getAllHospitals() {
        return new ArrayList<>(hospitals);
    }

    public List<Hospital> getNearbyHospitals(double lat, double lng, String specialty) {
        return hospitals.stream()
                .filter(hospital -> {
                    if (specialty != null && !specialty.isEmpty()) {
                        return hospital.getSpecialty().contains(specialty);
                    }
                    return true;
                })
                .peek(hospital -> {
                    double distance = calculateDistance(lat, lng, hospital.getLatitude(), hospital.getLongitude());
                    hospital.setDistance(distance);
                })
                .sorted((h1, h2) -> Double.compare(h1.getDistance(), h2.getDistance()))
                .limit(20)
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구의 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public Hospital getHospitalById(Long id) {
        return hospitals.stream()
                .filter(hospital -> hospital.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}

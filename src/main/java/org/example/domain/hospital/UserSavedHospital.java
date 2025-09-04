package org.example.domain.hospital;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_saved_hospital")
public class UserSavedHospital {

    @EmbeddedId
    private UserSavedHospitalId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // UserSavedHospitalId의 userId 필드에 매핑
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hospitalId") // UserSavedHospitalId의 hospitalId 필드에 매핑
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    // 사용자가 병원을 저장할 당시 검색했던 진료과목
    private String specialty;

    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt;

    @PrePersist
    protected void onSave() {
        this.savedAt = LocalDateTime.now();
    }

    // 생성자
    public UserSavedHospital(User user, Hospital hospital, String specialty) {
        this.id = new UserSavedHospitalId(user.getId(), hospital.getId());
        this.user = user;
        this.hospital = hospital;
        this.specialty = specialty;
    }
}

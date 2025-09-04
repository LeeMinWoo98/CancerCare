package org.example.domain.hospital;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class UserSavedHospitalId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "hospital_id")
    private Long hospitalId;

    public UserSavedHospitalId(Long userId, Long hospitalId) {
        this.userId = userId;
        this.hospitalId = hospitalId;
    }
}

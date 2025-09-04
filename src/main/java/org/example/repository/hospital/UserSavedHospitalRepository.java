package org.example.repository.hospital;

import org.example.domain.hospital.UserSavedHospital;
import org.example.domain.hospital.UserSavedHospitalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSavedHospitalRepository extends JpaRepository<UserSavedHospital, UserSavedHospitalId> {
    @Query("SELECT ush FROM UserSavedHospital ush JOIN FETCH ush.hospital WHERE ush.user.id = :userId")
    List<UserSavedHospital> findHospitalsByUserId(@Param("userId") Long userId);
}

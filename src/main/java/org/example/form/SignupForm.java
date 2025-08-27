package org.example.form;

import org.example.domain.User;
import java.time.LocalDate;

public record SignupForm(
    String loginId,
    String name,
    String email,
    String password,
    String passwordConfirm,
    String gender,
    LocalDate birthdate,
    String verificationCode
) {
    public User.Gender getGenderEnum() {
        if (gender == null) return User.Gender.N;
        return switch (gender.toUpperCase()) {
            case "MALE", "M" -> User.Gender.M;
            case "FEMALE", "F" -> User.Gender.F;
            default -> User.Gender.N;
        };
    }
}

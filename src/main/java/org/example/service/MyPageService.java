package org.example.service;

import org.example.domain.UserProfile;
import org.example.form.MyPageForm;
import org.example.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyPageService {
    private final UserProfileRepository userProfileRepository;

    public MyPageService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public UserProfile getProfile(Long userId) {
        return userProfileRepository.findById(userId).orElse(null);
    }

    @Transactional
    public void saveProfile(Long userId, MyPageForm form) {
        // 화면별 규칙: 마이페이지는 nullable 허용, 입력된 경우만 검증(간단 범위 체크)
        if (form.getHeightCm() != null) {
            int h = form.getHeightCm();
            if (h < 50 || h > 300) throw new IllegalArgumentException("키는 50에서 300 사이여야 합니다.");
        }
        if (form.getWeightKg() != null) {
            int w = form.getWeightKg();
            if (w < 10 || w > 400) throw new IllegalArgumentException("몸무게는 10에서 400 사이여야 합니다.");
        }

        UserProfile profile = userProfileRepository.findById(userId).orElse(new UserProfile(userId));
        profile.setCancerType(form.getCancerType());
        profile.setStage(form.getStage());
        profile.setHeightCm(form.getHeightCm());
        profile.setWeightKg(form.getWeightKg());
        userProfileRepository.save(profile);
    }
}

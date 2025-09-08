package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.domain.User;
import org.example.domain.UserProfile;
import org.example.dto.UserProfileView;
import org.example.form.MyPageForm;
import org.example.form.PasswordChangeForm;
import org.example.repository.UserRepository;
import org.example.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MyPageController {
    private final MyPageService myPageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MyPageController(MyPageService myPageService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.myPageService = myPageService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/mypage")
    public String getMyPage(
            @AuthenticationPrincipal UserDetails principal,
            Model model
    ) {
        Long userId = resolveUserId(principal);
        User user = userRepository.findById(userId).orElseThrow();
        UserProfile profile = myPageService.getProfile(userId);

        UserProfileView view = new UserProfileView();
        // 사용자 기본 정보
        view.setName(user.getName());
        view.setLoginId(user.getLoginId());
        view.setEmail(user.getEmail());
        view.setGender(user.getGender());
        view.setBirthdate(user.getBirthdate());
        view.setAge(calculateAge(user.getBirthdate()));
        
        // 프로필 정보
        if (profile != null) {
            view.setCancerType(profile.getCancerType());
            view.setStage(profile.getStage());
            view.setHeightCm(profile.getHeightCm());
            view.setWeightKg(profile.getWeightKg());
        }

        // 폼 객체도 프리필
        MyPageForm form = new MyPageForm();
        form.setName(user.getName());
        form.setEmail(user.getEmail());
        form.setGender(user.getGender());
        form.setLoginId(user.getLoginId());
        form.setBirthdate(user.getBirthdate());
        if (profile != null) {
            form.setCancerType(profile.getCancerType());
            form.setStage(profile.getStage());
            form.setHeightCm(profile.getHeightCm());
            form.setWeightKg(profile.getWeightKg());
        }

        model.addAttribute("profile", view);
        model.addAttribute("profileForm", form);
        model.addAttribute("passwordForm", new PasswordChangeForm());
        return "mypage/index";
    }

    @PostMapping("/mypage")
    public String saveMyPage(
            @AuthenticationPrincipal UserDetails principal,
            @ModelAttribute("profileForm") MyPageForm form,
            RedirectAttributes redirectAttributes
    ) {
        System.out.println("=== saveMyPage 시작 ===");
        try {
            Long userId = resolveUserId(principal);
            User user = userRepository.findById(userId).orElseThrow();
            
            // 기본 정보 업데이트
            user.setName(form.getName());
            user.setEmail(form.getEmail());
            user.setGender(form.getGender());
            user.setLoginId(form.getLoginId());
            user.setBirthdate(form.getBirthdate());
            userRepository.save(user);
            
            // 의료 정보 업데이트
            myPageService.saveProfile(userId, form);
            
            redirectAttributes.addFlashAttribute("message", "정보가 저장되었습니다.");
            System.out.println("=== 저장 성공, /main으로 리다이렉트 시도 ===");
        } catch (Exception e) {
            System.out.println("=== 에러 발생: " + e.getMessage() + " ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/main";
    }

    @PostMapping("/mypage/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @ModelAttribute PasswordChangeForm form,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        System.out.println("=== 비밀번호 변경 시작 ===");
        try {
            User user = userRepository.findByLoginId(principal.getUsername()).orElseThrow();
            
            // 현재 비밀번호 확인
            if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
                redirectAttributes.addFlashAttribute("passwordError", "현재 비밀번호가 일치하지 않습니다.");
                return "redirect:/mypage";
            }
            
            // 비밀번호 변경
            user.setPassword(passwordEncoder.encode(form.getNewPassword()));
            userRepository.save(user);
            
            System.out.println("=== 비밀번호 변경 성공, 로그아웃 처리 시작 ===");
            
            // 로그아웃 처리
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
            
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
            System.out.println("=== 로그아웃 완료, /main으로 리다이렉트 ===");
            return "redirect:/main";
        } catch (Exception e) {
            System.out.println("=== 비밀번호 변경 중 에러: " + e.getMessage() + " ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("passwordError", "비밀번호 변경 중 오류가 발생했습니다.");
            return "redirect:/mypage";
        }
    }

    // 실시간 비밀번호 검증 API
    @PostMapping("/api/verify-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, String> request
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentPassword = request.get("currentPassword");
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                response.put("valid", false);
                response.put("message", "");
                return ResponseEntity.ok(response);
            }
            
            User user = userRepository.findByLoginId(principal.getUsername()).orElseThrow();
            boolean isValid = passwordEncoder.matches(currentPassword, user.getPassword());
            
            response.put("valid", isValid);
            response.put("message", isValid ? "비밀번호가 일치합니다." : "현재 비밀번호가 일치하지 않습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "비밀번호 확인 중 오류가 발생했습니다.");
            return ResponseEntity.ok(response);
        }
    }

    private Long resolveUserId(UserDetails principal) {
        User user = userRepository.findByLoginId(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getId();
    }
    
    private int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}

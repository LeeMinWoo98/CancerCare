package org.example.controller;

import org.example.domain.User;
import org.example.domain.UserProfile;
import org.example.dto.UserProfileView;
import org.example.form.MyPageForm;
import org.example.form.PasswordChangeForm;
import org.example.repository.UserRepository;
import org.example.service.MyPageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage";
        }
        return "redirect:/main";
    }

    @PostMapping("/mypage/validate-password")
    @ResponseBody
    public Map<String, Boolean> validatePassword(@RequestBody Map<String, String> request, 
                                                 @AuthenticationPrincipal UserDetails principal) {
        String inputPassword = request.get("password");
        User user = userRepository.findByLoginId(principal.getUsername()).orElse(null);
        
        boolean isValid = false;
        if (user != null && passwordEncoder.matches(inputPassword, user.getPassword())) {
            isValid = true;
        }
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return response;
    }

    @PostMapping("/mypage/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @ModelAttribute PasswordChangeForm form,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
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
            
            // 로그아웃 처리
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, null);
            
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", "비밀번호 변경 중 오류가 발생했습니다.");
            return "redirect:/mypage";
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

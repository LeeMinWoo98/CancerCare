package org.example.controller;

import org.example.service.FindService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.security.PermitAll;

@Controller
@RequestMapping("/find")
public class FindController {
    private final FindService findService;

    public FindController(FindService findService) {
        this.findService = findService;
    }

    // 아이디/비밀번호 찾기 선택 페이지
    @PermitAll
    @GetMapping("")
    public String findPage() {
        return "find/find";
    }

    // 아이디 찾기 페이지
    @GetMapping("/login_id")
    public String findLoginIdPage() {
        return "find/find_login_id";
    }

    // 아이디 찾기 처리
    @PostMapping("/login_id")
    @ResponseBody
    public String findLoginId(@RequestParam String email) {
        try {
            if (!isValidEmail(email)) {
                return "유효하지 않은 이메일입니다.";
            }
            
            String maskedLoginId = findService.findLoginIdByEmail(email);
            if (maskedLoginId != null) {
                return "success:" + maskedLoginId;
            } else {
                return "가입되지 않은 이메일입니다.";
            }
        } catch (Exception e) {
            return "서버 오류가 발생했습니다.";
        }
    }

    // 비밀번호 재설정 페이지
    @GetMapping("/password")
    public String findPasswordPage() {
        return "find/find_password";
    }

    // 비밀번호 재설정 - 인증코드 발송
    @PostMapping("/password/send-code")
    @ResponseBody
    public String sendPasswordResetCode(@RequestParam String email) {
        try {
            if (!isValidEmail(email)) {
                return "유효하지 않은 이메일입니다.";
            }
            
            if (findService.sendPasswordResetCode(email)) {
                return "success";
            } else {
                return "가입되지 않은 이메일입니다.";
            }
        } catch (Exception e) {
            return "인증번호 전송에 실패했습니다.";
        }
    }

    // 비밀번호 재설정 - 인증코드 검증
    @PostMapping("/password/verify-code")
    @ResponseBody
    public String verifyPasswordResetCode(@RequestParam String email, @RequestParam String code) {
        boolean verified = findService.verifyPasswordResetCode(email, code);
        return verified ? "success" : "인증번호가 일치하지 않습니다.";
    }

    // 비밀번호 재설정 - 새 비밀번호 설정
    @PostMapping("/password/reset")
    @ResponseBody
    public String resetPassword(@RequestParam String email, @RequestParam String code, 
                               @RequestParam String newPassword, @RequestParam String confirmPassword) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                return "비밀번호가 일치하지 않습니다.";
            }
            
            if (newPassword.length() < 4) {
                return "비밀번호는 최소 4자 이상이어야 합니다.";
            }
            
            if (findService.resetPassword(email, code, newPassword)) {
                return "success";
            } else {
                return "비밀번호 변경에 실패했습니다.";
            }
        } catch (Exception e) {
            return "서버 오류가 발생했습니다.";
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}

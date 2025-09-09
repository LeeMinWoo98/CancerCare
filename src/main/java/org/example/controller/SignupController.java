package org.example.controller;

import org.example.form.SignupForm;
import org.example.service.SignupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/*@Controller
public class SignupController {
    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup/send-code")
    @ResponseBody
    public String sendCode(@RequestParam String email) {
        try {
            // 이메일 형식 검증
            if (!isValidEmail(email)) {
                return "유효하지 않은 이메일입니다.";
            }
            
            if (signupService.sendVerificationCode(email)) {
                return "success";
            } else {
                return "이미 사용 중인 이메일입니다.";
            }
        } catch (Exception e) {
            System.out.println("인증번호 전송 오류: " + e.getMessage());
            e.printStackTrace();
            return "인증번호 전송에 실패했습니다.";
        }
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @PostMapping("/signup/verify-code")
    @ResponseBody
    public String verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean verified = signupService.verifyCode(email, code);
        return verified ? "success" : "인증번호가 일치하지 않습니다.";
    }

    @PostMapping("/signup")
    public String signup(SignupForm form, Model model) {
        boolean success = signupService.signup(form);
        if (success) {
            return "redirect:/login?signup=success";
        } else {
            model.addAttribute("error", "회원가입에 실패했습니다.");
            return "signup";
        }
    }
}*/


@Controller
public class SignupController {
    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup/send-code")
    @ResponseBody
    // @RequestParam에 "email"이라고 명시적으로 파라미터 이름을 지정합니다.
    public String sendCode(@RequestParam("email") String email) {
        try {
            // 이메일 형식 검증
            if (!isValidEmail(email)) {
                return "유효하지 않은 이메일입니다.";
            }
            
            if (signupService.sendVerificationCode(email)) {
                return "success";
            } else {
                return "이미 사용 중인 이메일입니다.";
            }
        } catch (Exception e) {
            System.out.println("인증번호 전송 오류: " + e.getMessage());
            e.printStackTrace();
            return "인증번호 전송에 실패했습니다.";
        }
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @PostMapping("/signup/verify-code")
    @ResponseBody
    // 여기도 마찬가지로 "email", "code" 파라미터 이름을 명시적으로 지정합니다.
    public String verifyCode(@RequestParam("email") String email, @RequestParam("code") String code) {
        boolean verified = signupService.verifyCode(email, code);
        return verified ? "success" : "인증번호가 일치하지 않습니다.";
    }

    @PostMapping("/signup")
    public String signup(SignupForm form, Model model) {
        boolean success = signupService.signup(form);
        if (success) {
            return "redirect:/login?signup=success";
        } else {
            model.addAttribute("error", "회원가입에 실패했습니다.");
            return "signup";
        }
    }
}

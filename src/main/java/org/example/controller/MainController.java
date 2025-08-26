package org.example.controller;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "main";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/signup")
    public String sign(){
        return "signup";
    }

    @GetMapping("/chat")
    public String chat() { // 모델에 데이터를 추가할 필요가 없으므로 파라미터가 비어있어도 됩니다.
        return "chat";
    }
    
//    @GetMapping("/diagnosis")
//    public String diag() {
//    	return "diagnosis";
//    }


}

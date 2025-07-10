package com.dataury.soloJ.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html 렌더링
    }
}

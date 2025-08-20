package com.dataury.soloJ.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/oauth2/authorization/kakao";
    }
}

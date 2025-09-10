package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.User;
import com.sapehia.Geekbot.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/index")
    public String home() {
        return "index";
    }
    @PostMapping("/register")
    public String register(@ModelAttribute User user){
        authService.register(user);
        return "index";
    }
}

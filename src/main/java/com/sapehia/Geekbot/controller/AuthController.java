package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.User;
import com.sapehia.Geekbot.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/index")
    public String home() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User oauthUser, Model model) {
        Map<String, Object> userAttributes = oauthUser.getAttributes();
        List<Map<String, Object>> guilds = (List<Map<String, Object>>) userAttributes.get("guilds");

        System.out.println("Logged in as: " + userAttributes.get("username") + "#" + userAttributes.get("discriminator"));
        System.out.println("User ID: " + userAttributes.get("id"));

        if (guilds != null) {
            System.out.println("Guilds:");
            for (Map<String, Object> guild : guilds) {
                System.out.println(" - " + guild.get("name") + " | Admin/Owner: " + guild.get("isAdminOrOwner"));
            }
        }

        model.addAttribute("user", userAttributes);
        model.addAttribute("guilds", guilds);

        return "dashboard";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user){
        authService.register(user);
        return "index";
    }
}

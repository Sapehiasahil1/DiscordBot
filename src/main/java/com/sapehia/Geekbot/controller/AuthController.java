package com.sapehia.Geekbot.controller;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    private final JDA jda;

    public AuthController(JDA jda) {
        this.jda = jda;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User oauthUser, Model model) {
        Map<String, Object> userAttributes = oauthUser.getAttributes();
        List<Map<String, Object>> guilds = (List<Map<String, Object>>) userAttributes.get("guilds");

        System.out.println("Logged in as: " + userAttributes.get("username") + "#" + userAttributes.get("discriminator"));
        System.out.println("User ID: " + userAttributes.get("id"));

        Set<String> botGuildIds = jda.getGuilds()
                .stream()
                .map(Guild::getId)
                .collect(Collectors.toSet());

        List<Map<String, Object>> filteredGuilds = new ArrayList<>();
        boolean hasAdminGuilds = false;

        if (guilds != null) {
            for (Map<String, Object> guild : guilds) {
                boolean isAdminOrOwner = (Boolean) guild.get("isAdminOrOwner");
                String guildId = (String) guild.get("id");

                if (isAdminOrOwner && botGuildIds.contains(guildId)) {
                    filteredGuilds.add(guild);
                    hasAdminGuilds = true;
                }
            }
        }

        model.addAttribute("user", userAttributes);
        model.addAttribute("guilds", filteredGuilds);
        model.addAttribute("hasAdminGuilds", hasAdminGuilds);

        return "dashboard";
    }
}

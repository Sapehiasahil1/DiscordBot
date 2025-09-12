package com.sapehia.Geekbot.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

import java.util.*;

public class DiscordOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final RestTemplate restTemplate;

    public DiscordOAuth2UserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String userInfoUri = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
        if (!StringUtils.hasText(userInfoUri)) {
            throw new OAuth2AuthenticationException("Missing user info URI for Discord");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());
        headers.set("User-Agent", "MyApp (contact@yourdomain.com)");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, Map.class);
        Map<String, Object> userAttributes = (Map<String, Object>) userResponse.getBody();

        String guildsUri = "https://discord.com/api/users/@me/guilds";
        try {
            ResponseEntity<List> guildsResponse = restTemplate.exchange(guildsUri, HttpMethod.GET, entity, List.class);
            List<Map<String, Object>> guilds = guildsResponse.getBody();
            if (guilds != null) {
                for (Map<String, Object> guild : guilds) {
                    Object permObj = guild.get("permissions");
                    long perms = 0L;

                    if (permObj != null) {
                        try {
                            if (permObj instanceof String) {
                                perms = Long.parseLong((String) permObj);
                            } else if (permObj instanceof Number) {
                                perms = ((Number) permObj).longValue();
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    boolean hasAdministrator = (perms & 0x8L) == 0x8L;
                    boolean isOwner = Boolean.TRUE.equals(guild.get("owner"));
                    guild.put("isAdminOrOwner", isOwner || hasAdministrator);
                }

                userAttributes.put("guilds", guilds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DefaultOAuth2User(
                Collections.singleton(() -> "ROLE_USER"),
                userAttributes,
                "id"
        );
    }
}
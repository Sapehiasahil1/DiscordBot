package com.sapehia.Geekbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscordOAuth2UserServiceTest {

    private RestTemplate restTemplate;
    private DiscordOAuth2UserService userService;

    @BeforeEach
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        userService = new DiscordOAuth2UserService(restTemplate);
    }

    private OAuth2UserRequest createOAuth2UserRequest(String userInfoUri) {
        ClientRegistration.ProviderDetails.UserInfoEndpoint userInfoEndpoint = mock(ClientRegistration.ProviderDetails.UserInfoEndpoint.class);
        when(userInfoEndpoint.getUri()).thenReturn(userInfoUri);

        ClientRegistration.ProviderDetails providerDetails = mock(ClientRegistration.ProviderDetails.class);
        when(providerDetails.getUserInfoEndpoint()).thenReturn(userInfoEndpoint);

        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(clientRegistration.getProviderDetails()).thenReturn(providerDetails);

        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "dummy-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        return new OAuth2UserRequest(clientRegistration, token);
    }

    @Test
    public void testLoadUser_Successful() {
        OAuth2UserRequest userRequest = createOAuth2UserRequest("https://discord.com/api/users/@me");

        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put("id", "123456789");
        userAttributes.put("username", "testuser");

        ResponseEntity<Map> userInfoResponse = new ResponseEntity<>(userAttributes, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("https://discord.com/api/users/@me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(userInfoResponse);

        Map<String, Object> guild1 = new HashMap<>();
        guild1.put("id", "guild1");
        guild1.put("permissions", 0x8L);
        guild1.put("owner", false);

        Map<String, Object> guild2 = new HashMap<>();
        guild2.put("id", "guild2");
        guild2.put("permissions", 0x0L);
        guild2.put("owner", true);

        List<Map<String, Object>> guildsList = Arrays.asList(guild1, guild2);
        ResponseEntity<List> guildsResponse = new ResponseEntity<>(guildsList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("https://discord.com/api/users/@me/guilds"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        )).thenReturn(guildsResponse);

        OAuth2User user = userService.loadUser(userRequest);

        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("123456789");

        List<Map<String, Object>> guilds = (List<Map<String, Object>>) user.getAttributes().get("guilds");
        assertThat(guilds).hasSize(2);
        assertThat(guilds.get(0).get("isAdminOrOwner")).isEqualTo(true);
        assertThat(guilds.get(1).get("isAdminOrOwner")).isEqualTo(true);
    }

    @Test
    public void testLoadUser_GuildsRequestFails_StillReturnsUser() {
        OAuth2UserRequest userRequest = createOAuth2UserRequest("https://discord.com/api/users/@me");

        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put("id", "user123");

        ResponseEntity<Map> userInfoResponse = new ResponseEntity<>(userAttributes, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("https://discord.com/api/users/@me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(userInfoResponse);

        when(restTemplate.exchange(
                eq("https://discord.com/api/users/@me/guilds"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        )).thenThrow(new RuntimeException("API failure"));

        OAuth2User user = userService.loadUser(userRequest);

        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("user123");

        assertThat(user.getAttributes()).doesNotContainKey("guilds");
    }
}

package com.sapehia.Geekbot.controller;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private JDA jda;

    @Mock
    private OAuth2User oauthUser;

    @InjectMocks
    private AuthController authController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
    }

    @Test
    void testHome_ReturnsIndexView() {
        String viewName = authController.home();

        assertEquals("index", viewName);
    }

    @Test
    void testDashboard_UserIsAdminOfBotGuilds_GuildsAreFiltered() {
        Guild botGuild1 = mock(Guild.class);
        when(botGuild1.getId()).thenReturn("111");

        Guild botGuild2 = mock(Guild.class);
        when(botGuild2.getId()).thenReturn("222");

        when(jda.getGuilds()).thenReturn(Arrays.asList(botGuild1, botGuild2));

        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put("username", "testuser");
        userAttributes.put("discriminator", "1234");
        userAttributes.put("id", "user-id");

        List<Map<String, Object>> userGuilds = new ArrayList<>();

        Map<String, Object> adminGuild = new HashMap<>();
        adminGuild.put("id", "111");
        adminGuild.put("isAdminOrOwner", true);
        userGuilds.add(adminGuild);

        Map<String, Object> nonAdminGuild = new HashMap<>();
        nonAdminGuild.put("id", "111");
        nonAdminGuild.put("isAdminOrOwner", false);
        userGuilds.add(nonAdminGuild);

        Map<String, Object> outsideGuild = new HashMap<>();
        outsideGuild.put("id", "333");
        outsideGuild.put("isAdminOrOwner", true);
        userGuilds.add(outsideGuild);

        userAttributes.put("guilds", userGuilds);
        when(oauthUser.getAttributes()).thenReturn(userAttributes);

        String viewName = authController.dashboard(oauthUser, model);

        assertEquals("dashboard", viewName);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> filteredGuilds = (List<Map<String, Object>>) model.getAttribute("guilds");

        assertNotNull(filteredGuilds);
        assertEquals(1, filteredGuilds.size());
        assertEquals("111", filteredGuilds.get(0).get("id"));

        assertTrue((Boolean) model.getAttribute("hasAdminGuilds"));
        assertEquals(userAttributes, model.getAttribute("user"));
    }

    @Test
    void testDashboard_UserHasNoAdminBotGuilds_HasAdminGuildsIsFalse() {
        Guild botGuild = mock(Guild.class);
        when(botGuild.getId()).thenReturn("111");
        when(jda.getGuilds()).thenReturn(Collections.singletonList(botGuild));

        Map<String, Object> userAttributes = new HashMap<>();
        List<Map<String, Object>> userGuilds = new ArrayList<>();

        Map<String, Object> userGuild = new HashMap<>();
        userGuild.put("id", "111");
        userGuild.put("isAdminOrOwner", false);
        userGuilds.add(userGuild);

        userAttributes.put("guilds", userGuilds);
        when(oauthUser.getAttributes()).thenReturn(userAttributes);

        String viewName = authController.dashboard(oauthUser, model);

        assertEquals("dashboard", viewName);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> filteredGuilds = (List<Map<String, Object>>) model.getAttribute("guilds");

        assertNotNull(filteredGuilds);
        assertTrue(filteredGuilds.isEmpty());
        assertFalse((Boolean) model.getAttribute("hasAdminGuilds"));
    }

    @Test
    void testDashboard_UserHasNoGuildsAttribute_EmptyGuildsList() {
        when(jda.getGuilds()).thenReturn(Collections.emptyList());

        Map<String, Object> userAttributes = new HashMap<>();
        when(oauthUser.getAttributes()).thenReturn(userAttributes);

        String viewName = authController.dashboard(oauthUser, model);

        assertEquals("dashboard", viewName);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> filteredGuilds = (List<Map<String, Object>>) model.getAttribute("guilds");

        assertNotNull(filteredGuilds);
        assertTrue(filteredGuilds.isEmpty());
        assertFalse((Boolean) model.getAttribute("hasAdminGuilds"));
    }
}
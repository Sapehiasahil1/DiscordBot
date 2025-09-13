package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;
import com.sapehia.Geekbot.model.ServerConfigForm;
import com.sapehia.Geekbot.service.ServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerControllerTest {

    @Mock
    private ServerService serverService;

    @InjectMocks
    private ServerController serverController;

    private String serverId;
    private Model model;
    private Server server;

    @BeforeEach
    void setUp() {
        serverId = "server-123";
        model = new ConcurrentModel();

        server = new Server();
        server.setServerId(serverId);
        server.setServerName("Test Server");
        server.setQuestionTime(LocalTime.of(9, 30));
    }

    @Test
    void testServerHome_ReturnsCorrectViewAndAddsServerDetailsToModel() {
        // Given
        when(serverService.getServerById(serverId)).thenReturn(server);

        // When
        String viewName = serverController.serverHome(serverId, model);

        // Then
        assertEquals("server-home", viewName);
        assertEquals(serverId, model.getAttribute("serverId"));
        assertEquals("Test Server", model.getAttribute("serverName"));
        verify(serverService, times(1)).getServerById(serverId);
    }

    @Test
    void testServerConfig_WithExistingQuestionsAndExcludedDays() {
        // Given
        Question q1 = new Question();
        q1.setText("Question 1?");
        Question q2 = new Question();
        q2.setText("Question 2?");
        List<Question> questions = List.of(q1, q2);
        Set<DayOfWeek> excludedDays = new HashSet<>(List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        server.setExcludedDaysFromSet(excludedDays);

        when(serverService.getServerById(serverId)).thenReturn(server);
        when(serverService.getQuestionFromServer(serverId)).thenReturn(questions);

        // When
        String viewName = serverController.serverConfig(serverId, model);

        // Then
        assertEquals("server-config", viewName);
        assertEquals(serverId, model.getAttribute("serverId"));
        assertEquals("Test Server", model.getAttribute("serverName"));

        ServerConfigForm form = (ServerConfigForm) model.getAttribute("serverConfigForm");
        assertNotNull(form);
        assertEquals(LocalTime.of(9, 30), form.getSendTime());
        assertEquals(2, form.getQuestions().size());
        assertEquals(excludedDays, form.getExcludedDays());
        assertTrue(form.getQuestions().contains("Question 1?"));

        verify(serverService, times(1)).getServerById(serverId);
        verify(serverService, times(1)).getQuestionFromServer(serverId);
    }

    @Test
    void testServerConfig_WithoutExistingQuestions() {
        // Given
        when(serverService.getServerById(serverId)).thenReturn(server);
        when(serverService.getQuestionFromServer(serverId)).thenReturn(Collections.emptyList());

        // When
        String viewName = serverController.serverConfig(serverId, model);

        // Then
        assertEquals("server-config", viewName);

        ServerConfigForm form = (ServerConfigForm) model.getAttribute("serverConfigForm");
        assertNotNull(form);
        assertEquals(Collections.emptyList(), form.getQuestions());

        verify(serverService, times(1)).getServerById(serverId);
        verify(serverService, times(1)).getQuestionFromServer(serverId);
    }

    @Test
    void testSaveServerConfig_Success() {
        // Given
        ServerConfigForm form = new ServerConfigForm();
        form.setSendTime(LocalTime.of(10, 0));
        form.setQuestions(List.of("New Question 1?"));
        form.setExcludedDays(Set.of(DayOfWeek.SATURDAY));

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        when(serverService.getServerById(serverId)).thenReturn(server);

        // When
        String viewName = serverController.saveServerConfig(serverId, form, redirectAttributes);

        // Then
        verify(serverService, times(1)).saveServerConfig(
                serverId,
                form.getSendTime(),
                form.getQuestions(),
                form.getExcludedDays()
        );

        assertEquals("redirect:/server/" + serverId, viewName);
        assertEquals(serverId, redirectAttributes.getAttribute("serverId"));
        assertEquals(server.getServerName(), redirectAttributes.getAttribute("serverName"));
        assertEquals(true, redirectAttributes.getFlashAttributes().get("success"));
    }

    @Test
    void testSaveServerConfig_WithNullExcludedDays() {
        // Given
        ServerConfigForm form = new ServerConfigForm();
        form.setSendTime(LocalTime.of(10, 0));
        form.setQuestions(List.of("New Question 1?"));
        form.setExcludedDays(null); // Explicitly setting to null

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        when(serverService.getServerById(serverId)).thenReturn(server);

        // When
        String viewName = serverController.saveServerConfig(serverId, form, redirectAttributes);

        // Then
        verify(serverService, times(1)).saveServerConfig(
                eq(serverId),
                eq(form.getSendTime()),
                eq(form.getQuestions()),
                any(HashSet.class) // Verify that a new HashSet is passed
        );

        assertEquals("redirect:/server/" + serverId, viewName);
    }
}
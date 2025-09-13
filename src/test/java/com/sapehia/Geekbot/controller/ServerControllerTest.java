package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.Question;
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

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

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

    @BeforeEach
    void setUp() {
        serverId = "server-123";
        model = new ConcurrentModel();
    }

    @Test
    void testServerHome_ReturnsCorrectViewAndAddsServerIdToModel() {
        String viewName = serverController.serverHome(serverId, model);

        assertEquals("server-home", viewName);
        assertEquals(serverId, model.getAttribute("serverId"));
    }

    @Test
    void testServerConfig_WithExistingQuestions() {
        Question q1 = new Question();
        q1.setText("Question 1?");
        Question q2 = new Question();
        q2.setText("Question 2?");
        List<Question> questions = List.of(q1, q2);

        when(serverService.getQuestionFromServer(serverId)).thenReturn(questions);

        String viewName = serverController.serverConfig(serverId, model);

        assertEquals("server-config", viewName);
        assertEquals(serverId, model.getAttribute("serverId"));

        ServerConfigForm form = (ServerConfigForm) model.getAttribute("serverConfigForm");
        assertNotNull(form);
        assertEquals(LocalTime.of(9, 30), form.getSendTime());
        assertEquals(2, form.getQuestions().size());
        assertTrue(form.getQuestions().contains("Question 1?"));
        assertTrue(form.getQuestions().contains("Question 2?"));

        verify(serverService, times(1)).getQuestionFromServer(serverId);
    }

    @Test
    void testServerConfig_WithoutExistingQuestions() {
        when(serverService.getQuestionFromServer(serverId)).thenReturn(Collections.emptyList());

        String viewName = serverController.serverConfig(serverId, model);

        assertEquals("server-config", viewName);

        ServerConfigForm form = (ServerConfigForm) model.getAttribute("serverConfigForm");
        assertNotNull(form);
        assertEquals(Collections.emptyList(), form.getQuestions());

        verify(serverService, times(1)).getQuestionFromServer(serverId);
    }

    @Test
    void testSaveServerConfig_Success() {
        // Given
        ServerConfigForm form = new ServerConfigForm();
        form.setSendTime(LocalTime.of(10, 0));
        form.setQuestions(List.of("New Question 1?"));

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = serverController.saveServerConfig(serverId, form, redirectAttributes);

        verify(serverService, times(1)).saveServerConfig(serverId, form.getSendTime(), form.getQuestions());

        assertEquals("redirect:/server/" + serverId, viewName);
        assertEquals(serverId, redirectAttributes.getAttribute("serverId"));
        assertEquals(true, redirectAttributes.getFlashAttributes().get("success"));
    }
}
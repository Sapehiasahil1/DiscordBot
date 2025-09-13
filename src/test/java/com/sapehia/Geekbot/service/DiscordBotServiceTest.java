package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.QuestionAssignment;
import com.sapehia.Geekbot.model.Server;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscordBotServiceTest {

    @InjectMocks
    private DiscordBotService discordBotService;

    @Mock
    private QuestionService questionService;

    @Mock
    private QuestionAssignmentService questionAssignmentService;

    @Mock
    private JDA jda;

    @Mock
    private net.dv8tion.jda.api.entities.Member mockMember;

    @BeforeEach
    public void setup() {
        discordBotService.setJda(jda);
    }

    @Test
    void testCreateDefaultQuestions_CreatesAndAssignsAllQuestions() {
        Server server = new Server();
        server.setServerId("123");
        server.setServerName("Test Guild");

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);

        when(questionService.createQuestion(any(Question.class))).thenAnswer(invocation -> {
            Question q = invocation.getArgument(0);
            q.setId((long) (Math.random() * 10000));
            return q;
        });

        discordBotService.createDefaultQuestions(server);

        verify(questionService, times(3)).createQuestion(questionCaptor.capture());
        verify(questionAssignmentService, times(3)).save(any(QuestionAssignment.class));

        List<Question> captured = questionCaptor.getAllValues();
        assertThat(captured).hasSize(3);
        assertThat(captured).extracting("text").containsExactly(
                "What did you complete yesterday?",
                "What are your plans for today?",
                "Are you stuck anywhere?"
        );
    }

    @Test
    void testIsAdmin_WhenMemberHasAdminPermission_ReturnsTrue() {
        when(mockMember.hasPermission(Permission.ADMINISTRATOR)).thenReturn(true);
        boolean result = discordBotService.isAdmin(mockMember);
        assertThat(result).isTrue();
    }

    @Test
    void testIsAdmin_WhenMemberHasAdminRole_ReturnsTrue() {
        Role mockRole = mock(Role.class);
        when(mockRole.getName()).thenReturn("Admin");
        when(mockMember.hasPermission(Permission.ADMINISTRATOR)).thenReturn(false);
        when(mockMember.getRoles()).thenReturn(List.of(mockRole));

        boolean result = discordBotService.isAdmin(mockMember);
        assertThat(result).isTrue();
    }

    @Test
    void testIsAdmin_WhenMemberHasNoAdminRoleOrPermission_ReturnsFalse() {
        when(mockMember.hasPermission(Permission.ADMINISTRATOR)).thenReturn(false);
        when(mockMember.getRoles()).thenReturn(List.of());

        boolean result = discordBotService.isAdmin(mockMember);
        assertThat(result).isFalse();
    }
}

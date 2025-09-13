package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.QuestionAssignment;
import com.sapehia.Geekbot.model.Server;
import com.sapehia.Geekbot.repository.QuestionAssignmentRepository;
import com.sapehia.Geekbot.service.impl.QuestionAssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionAssignmentServiceImplTest {

    @Mock
    private QuestionAssignmentRepository questionAssignmentRepository;

    @InjectMocks
    private QuestionAssignmentServiceImpl questionAssignmentServiceImpl;

    private QuestionAssignment questionAssignment;
    private Server server;
    private Question question;
    private LocalDate assignDate;

    @BeforeEach
    public void setup() {
        server = new Server();
        server.setServerId("server-1");

        question = new Question();
        question.setId(1L);

        assignDate = LocalDate.now();

        questionAssignment = new QuestionAssignment();
        questionAssignment.setId(1L);
        questionAssignment.setServer(server);
        questionAssignment.setQuestion(question);
        questionAssignment.setDate(assignDate);
    }

    @Test
    void testSave() {
        when(questionAssignmentRepository.save(questionAssignment)).thenReturn(questionAssignment);

        QuestionAssignment savedQuestionAssignment = questionAssignmentServiceImpl.save(questionAssignment);

        assertNotNull(savedQuestionAssignment);
        assertEquals(questionAssignment.getId(), savedQuestionAssignment.getId());
        verify(questionAssignmentRepository, times(1)).save(questionAssignment);
    }

    @Test
    void testGetByServerIdAndQuestionIdAndAssignedDate_Found() {
        when(questionAssignmentRepository.findAssignment("server-1", 1L, assignDate))
                .thenReturn(Optional.of(questionAssignment));

        QuestionAssignment foundAssignment = questionAssignmentServiceImpl
                .getByServerIdAndQuestionIdAndAssignedDate("server-1", 1L, assignDate);

        assertNotNull(foundAssignment);
        assertEquals(questionAssignment.getId(), foundAssignment.getId());
        verify(questionAssignmentRepository, times(1)).findAssignment("server-1", 1L, assignDate);
    }

    @Test
    void testGetByServerIdAndQuestionIdAndAssignedDate_NotFound() {
        when(questionAssignmentRepository.findAssignment("server-123", 1L, assignDate))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> questionAssignmentServiceImpl.getByServerIdAndQuestionIdAndAssignedDate("server-123", 1L, assignDate)
        );

        assertEquals("not found question Assignment", exception.getMessage());
        verify(questionAssignmentRepository, times(1)).findAssignment("server-123", 1L, assignDate);
    }

    @Test
    void testGetAssignmentsByServerAndDate_Found() {
        String serverId = "server-1";
        LocalDate testDate = LocalDate.now();
        List<QuestionAssignment> assignments = List.of(questionAssignment);

        when(questionAssignmentRepository.findByServerServerIdAndDate(serverId, testDate)).thenReturn(assignments);

        List<QuestionAssignment> foundAssignments = questionAssignmentServiceImpl.getAssignmentsByServerAndDate(serverId, testDate);

        assertNotNull(foundAssignments);
        assertFalse(foundAssignments.isEmpty());
        assertEquals(1, foundAssignments.size());
        verify(questionAssignmentRepository, times(1)).findByServerServerIdAndDate(serverId, testDate);
    }

    @Test
    void testGetAssignmentsByServerAndDate_NotFound() {
        String serverId = "nonexistent-server";
        LocalDate testDate = LocalDate.now();

        when(questionAssignmentRepository.findByServerServerIdAndDate(serverId, testDate)).thenReturn(Collections.emptyList());

        List<QuestionAssignment> foundAssignments = questionAssignmentServiceImpl.getAssignmentsByServerAndDate(serverId, testDate);

        assertNotNull(foundAssignments);
        assertTrue(foundAssignments.isEmpty());
        verify(questionAssignmentRepository, times(1)).findByServerServerIdAndDate(serverId, testDate);
    }
}
package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;
import com.sapehia.Geekbot.repository.QuestionRepository;
import com.sapehia.Geekbot.service.impl.QuestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Question question1;
    private Question question2;
    private Server server;

    @BeforeEach
    void setUp() {
        server = new Server();
        server.setServerId("server-123");

        question1 = new Question();
        question1.setId(1L);
        question1.setText("What is Java?");
        question1.setServer(server);

        question2 = new Question();
        question2.setId(2L);
        question2.setText("What is Spring Boot?");
        question2.setServer(server);
    }

    @Test
    void testCreateQuestion() {
        when(questionRepository.save(question1)).thenReturn(question1);

        Question createdQuestion = questionService.createQuestion(question1);

        assertNotNull(createdQuestion);
        assertEquals(question1.getText(), createdQuestion.getText());
        verify(questionRepository, times(1)).save(question1);
    }

    @Test
    void testUpdateQuestion_Success() {
        String newText = "What is a microservice?";

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question1));
        when(questionRepository.save(any(Question.class))).thenReturn(question1);

        Question updatedQuestion = questionService.updateQuestion(1L, newText);

        assertNotNull(updatedQuestion);
        assertEquals(newText, updatedQuestion.getText());

        verify(questionRepository, times(1)).findById(1L);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    void testUpdateQuestion_NotFound() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> questionService.updateQuestion(99L, "New text")
        );

        assertEquals("Question not found with id 99", thrown.getMessage());
        verify(questionRepository, times(1)).findById(99L);
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void testDeleteQuestion() {
        questionService.deleteQuestion(1L);

        verify(questionRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetQuestionsForServer() {
        List<Question> serverQuestions = List.of(question1, question2);

        when(questionRepository.findByServer_ServerId("server-123")).thenReturn(serverQuestions);

        List<Question> foundQuestions = questionService.getQuestionsForServer("server-123");

        assertNotNull(foundQuestions);
        assertEquals(2, foundQuestions.size());
        assertEquals(question1.getText(), foundQuestions.getFirst().getText());
        verify(questionRepository, times(1)).findByServer_ServerId("server-123");
    }

    @Test
    void testGetQuestionById_Found() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question1));

        Question foundQuestion = questionService.getQuestionById(1L);

        assertNotNull(foundQuestion);
        assertEquals(1L, foundQuestion.getId());
        assertEquals("What is Java?", foundQuestion.getText());
        verify(questionRepository, times(1)).findById(1L);
    }

    @Test
    void testGetQuestionById_NotFound() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> questionService.getQuestionById(99L)
        );

        assertEquals("Question not found with id 99", thrown.getMessage());
        verify(questionRepository, times(1)).findById(99L);
    }
}
package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;

import java.time.LocalDate;
import java.util.List;

public interface QuestionService {
    Question createQuestion(Question question);
    Question updateQuestion(Long questionId, String newText);
    void deleteQuestion(Long questionId);
    List<Question> getQuestionsForServer(String serverId);
    Question getQuestionById(Long questionId);

    List<Question> findByServerAndDate(Server server, LocalDate date);
}

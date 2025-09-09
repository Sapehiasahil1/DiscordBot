package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.Question;

import java.util.List;

public interface QuestionService {
    Question createQuestion(String serverId, String text);
    Question updateQuestion(Long questionId, String newText);
    void deleteQuestion(Long questionId);
    List<Question> getQuestionsForServer(String serverId);
    Question getQuestionById(Long questionId);
}

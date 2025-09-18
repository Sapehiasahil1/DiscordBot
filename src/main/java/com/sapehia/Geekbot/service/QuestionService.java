package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;

import java.time.LocalDate;
import java.util.List;

public interface QuestionService {
    Question createQuestion(Question question);

    List<Question> getQuestionsForServer(String serverId);

    List<Question> findByServerAndDate(Server server, LocalDate date);
}

package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;
import com.sapehia.Geekbot.repository.QuestionRepository;
import com.sapehia.Geekbot.service.QuestionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public List<Question> getQuestionsForServer(String serverId) {
        return questionRepository.findByServer_ServerId(serverId);
    }

    @Override
    public List<Question> findByServerAndDate(Server server, LocalDate date) {
        return questionRepository.findQuestionsByServerAndDate(server, date);
    }
}

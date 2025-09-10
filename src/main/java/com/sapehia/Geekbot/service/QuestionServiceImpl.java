package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;
import com.sapehia.Geekbot.repository.QuestionRepository;
import com.sapehia.Geekbot.repository.ServerRepository;
import com.sapehia.Geekbot.service.impl.QuestionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final ServerRepository serverRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository, ServerRepository serverRepository) {
        this.questionRepository = questionRepository;
        this.serverRepository = serverRepository;
    }

    @Override
    public Question createQuestion(String serverId, String text) {
        Question question = new Question();

        Server server = serverRepository.findById(serverId)
                        .orElseThrow(()-> new RuntimeException("Server not found with id " + serverId));
        question.setServer(server);
        question.setText(text);

        return questionRepository.save(question);
    }

    @Override
    public Question updateQuestion(Long questionId, String newText) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(()-> new RuntimeException("Question not found with id " + questionId));

        question.setText(newText);

        return questionRepository.save(question);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    @Override
    public List<Question> getQuestionsForServer(String serverId) {
        return questionRepository.findByServer_ServerId(serverId);
    }

    @Override
    public Question getQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(()-> new RuntimeException("Quesetion not found with id " + questionId));
    }
}

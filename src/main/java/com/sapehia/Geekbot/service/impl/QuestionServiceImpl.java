package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.repository.QuestionRepository;
import com.sapehia.Geekbot.service.QuestionService;
import org.springframework.stereotype.Service;

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
                .orElseThrow(()-> new RuntimeException("Question not found with id " + questionId));
    }
}

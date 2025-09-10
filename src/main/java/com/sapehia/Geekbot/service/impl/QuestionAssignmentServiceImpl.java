package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.QuestionAssignment;
import com.sapehia.Geekbot.repository.QuestionAssignmentRepository;
import com.sapehia.Geekbot.service.QuestionAssignmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class QuestionAssignmentServiceImpl implements QuestionAssignmentService {

    private final QuestionAssignmentRepository questionAssignmentRepository;

    public QuestionAssignmentServiceImpl(QuestionAssignmentRepository questionAssignmentRepository) {
        this.questionAssignmentRepository = questionAssignmentRepository;
    }

    @Override
    public QuestionAssignment save(QuestionAssignment questionAssignment) {
        return questionAssignmentRepository.save(questionAssignment);
    }

    @Override
    public QuestionAssignment getByServerIdAndQuestionIdAndAssignedDate(String serverId, Long questionId, LocalDate assignedDate) {
        return questionAssignmentRepository.findAssignment(serverId, questionId, assignedDate)
                .orElseThrow(()-> new RuntimeException("not found question Assignment"));
    }
}

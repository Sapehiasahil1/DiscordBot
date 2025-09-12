package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.QuestionAssignment;

import java.time.LocalDate;
import java.util.List;

public interface QuestionAssignmentService {
    QuestionAssignment save(QuestionAssignment questionAssignment);
    QuestionAssignment getByServerIdAndQuestionIdAndAssignedDate(
            String serverId, Long questionId, LocalDate assignedDate
    );

    List<QuestionAssignment> getAssignmentsByServerAndDate(String serverId, LocalDate today);
}

package com.sapehia.Geekbot.repository;

import com.sapehia.Geekbot.model.QuestionAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionAssignmentRepository extends JpaRepository<QuestionAssignment, Long> {
    @Query("SELECT qa FROM QuestionAssignment qa " +
            "WHERE qa.server.serverId = :serverId " +
            "AND qa.question.id = :questionId " +
            "AND qa.date = :date")
    Optional<QuestionAssignment> findAssignment(String serverId, Long questionId, LocalDate date);

    List<QuestionAssignment> findByServerServerIdAndDate(String serverId, LocalDate date);
}

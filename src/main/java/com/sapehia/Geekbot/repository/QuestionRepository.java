package com.sapehia.Geekbot.repository;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByServer_ServerId(String serverId);

    @Query("SELECT DISTINCT qa.question FROM QuestionAssignment qa " +
            "WHERE qa.server = :server AND qa.date = :date")
    List<Question> findQuestionsByServerAndDate(@Param("server") Server server,
                                                @Param("date") LocalDate date);
}

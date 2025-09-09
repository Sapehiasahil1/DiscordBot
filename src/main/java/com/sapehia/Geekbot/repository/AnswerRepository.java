package com.sapehia.Geekbot.repository;

import com.sapehia.Geekbot.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer,Long> {

    @Query("SELECT a FROM Answer a WHERE DATE(a.submittedAt) = :date")
    List<Answer> findBySubmittedDate(@Param("date") LocalDate date);

}

package com.sapehia.Geekbot.repository;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    @Query("SELECT a FROM Answer a " +
            "JOIN FETCH a.assignment qass " +
            "JOIN FETCH qass.question q " +
            "WHERE qass.server.serverId = :serverId " +
            "AND qass.date = :date")
    List<Answer> findAllByServerIdAndDate(@Param("serverId") String serverId,
                                          @Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT qa.date) FROM Answer a JOIN a.assignment qa WHERE a.member.discordUserId = :memberId AND qa.server.serverId = :serverId AND qa.date BETWEEN :startDate AND :endDate")
    int countDistinctDaysByMemberAndServerAndDateBetween(
            @Param("memberId") String memberId,
            @Param("serverId") String serverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Answer findByMemberDiscordUserIdAndAssignmentId(String discordUserId, Long assignmentId);

    @Query("SELECT a FROM Answer a " +
            "WHERE a.assignment.server = :server AND a.assignment.date = :date")
    List<Answer> findAnswersByServerAndDate(@Param("server") Server server,
                                            @Param("date") LocalDate date);
}

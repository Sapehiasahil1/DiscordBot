package com.sapehia.Geekbot.repository;

import com.sapehia.Geekbot.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer,Long> {

    @Query("SELECT a FROM Answer a " +
            "WHERE a.submittedAt >= :dateStart " +
            "AND a.submittedAt < :dateEnd")
    List<Answer> findBySubmittedDate(@Param("dateStart") LocalDateTime dateStart,
                                     @Param("dateEnd") LocalDateTime dateEnd);

    @Query("SELECT a FROM Answer a " +
            "JOIN a.member m " +
            "JOIN m.servers s " +
            "WHERE s.serverId = :serverId " +
            "AND a.submittedAt >= :dateStart " +
            "AND a.submittedAt < :dateEnd")
    List<Answer> findByServerAndDate(@Param("serverId") String serverId,
                                     @Param("dateStart") LocalDateTime dateStart,
                                     @Param("dateEnd") LocalDateTime dateEnd);

    @Query("SELECT COUNT(DISTINCT FUNCTION('DATE', a.submittedAt)) " +
            "FROM Answer a " +
            "JOIN a.member m " +
            "JOIN m.servers s " +
            "WHERE s.serverId = :serverId " +
            "AND m.discordUserId = :memberId " +
            "AND a.submittedAt BETWEEN :start AND :end")
    long countAttendanceInPeriod(@Param("serverId") String serverId,
                                 @Param("memberId") String memberId,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);
}
